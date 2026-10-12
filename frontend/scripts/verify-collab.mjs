import { Client } from "@stomp/stompjs";
import WebSocket from "ws";

globalThis.WebSocket = WebSocket;

const BASE = "http://localhost:8080";
const suffix = Date.now();

async function login(email, password) {
  const res = await fetch(`${BASE}/api/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });
  if (!res.ok) {
    throw new Error(`Login failed for ${email}: ${await res.text()}`);
  }
  return res.text();
}

async function register(email, password) {
  const res = await fetch(`${BASE}/api/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });
  if (!res.ok) {
    throw new Error(`Register failed: ${await res.text()}`);
  }
}

async function createDoc(token, title) {
  const res = await fetch(`${BASE}/api/documents/createDocument`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ title, content: "Hello" }),
  });
  if (!res.ok) {
    throw new Error(`Create doc failed: ${await res.text()}`);
  }
  return res.json();
}

async function share(token, docId, email) {
  const params = new URLSearchParams({ email, role: "EDITOR" });
  const res = await fetch(
    `${BASE}/api/documents/sharePermission/${docId}?${params}`,
    {
      method: "POST",
      headers: { Authorization: `Bearer ${token}` },
    }
  );
  if (!res.ok) {
    throw new Error(`Share failed: ${await res.text()}`);
  }
}

function connectStomp(token) {
  return new Promise((resolve, reject) => {
    const client = new Client({
      brokerURL: "ws://localhost:8080/ws",
      connectHeaders: { Authorization: `Bearer ${token}` },
      onConnect: () => resolve(client),
      onStompError: (frame) => reject(new Error(frame.body)),
      onWebSocketError: (err) => reject(err),
    });
    client.activate();
  });
}

function waitForTopic(client, documentId, timeoutMs = 10000) {
  return new Promise((resolve, reject) => {
    const timer = setTimeout(
      () => reject(new Error("Timed out waiting for topic message")),
      timeoutMs
    );
    client.subscribe(`/topic/document/${documentId}`, (message) => {
      clearTimeout(timer);
      resolve(JSON.parse(message.body));
    });
  });
}

const emailA = `collab-a-${suffix}@test.local`;
const emailB = `collab-b-${suffix}@test.local`;
const password = "testpass123";

await register(emailA, password);
await register(emailB, password);

const tokenA = await login(emailA, password);
const tokenB = await login(emailB, password);

const doc = await createDoc(tokenA, "Collab verify");
await share(tokenA, doc.id, emailB);

const clientB = await connectStomp(tokenB);
const clientA = await connectStomp(tokenA);

const incoming = waitForTopic(clientB, doc.id);

clientA.publish({
  destination: "/app/document",
  body: JSON.stringify({
    documentId: doc.id,
    operation: "INSERT",
    position: 5,
    text: " world",
    version: doc.version ?? 0,
  }),
});

const payload = await incoming;

if (!payload.content?.includes("Hello world")) {
  throw new Error(`Unexpected content after sync: ${payload.content}`);
}

console.log("PASS: second session received collaborative update:", payload.content);

clientA.deactivate();
clientB.deactivate();
