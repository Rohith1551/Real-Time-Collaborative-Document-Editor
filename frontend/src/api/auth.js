import { apiFetch, setToken } from "./client.js";

export async function login(email, password) {
  const token = await apiFetch("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
  if (typeof token === "string") {
    setToken(token);
  }
  return token;
}

export async function register(email, password) {
  return apiFetch("/api/auth/register", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
}

export function logout() {
  setToken(null);
}
