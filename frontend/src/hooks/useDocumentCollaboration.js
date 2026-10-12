import { Client } from "@stomp/stompjs";
import { useCallback, useEffect, useRef, useState } from "react";
import { getToken } from "../api/client.js";
import { wsUrl } from "../api/config.js";
import { findTextChange } from "../utils/textDiff.js";

export function useDocumentCollaboration({
  documentId,
  enabled,
  readOnly,
  initialContent,
  initialVersion,
  onConflict,
}) {
  const [content, setContent] = useState(initialContent ?? "");
  const [version, setVersion] = useState(initialVersion ?? null);
  const [connected, setConnected] = useState(false);
  const [saveState, setSaveState] = useState("saved"); // saved | saving | error

  const clientRef = useRef(null);
  const contentRef = useRef(initialContent ?? "");
  const versionRef = useRef(initialVersion ?? null);
  const suppressSendRef = useRef(false);
  const pendingOpsRef = useRef(0);

  useEffect(() => {
    contentRef.current = initialContent ?? "";
    setContent(initialContent ?? "");
  }, [initialContent]);

  useEffect(() => {
    versionRef.current = initialVersion ?? null;
    setVersion(initialVersion ?? null);
  }, [initialVersion]);

  const applyRemote = useCallback((newContent, newVersion) => {
    suppressSendRef.current = true;
    contentRef.current = newContent;
    versionRef.current = newVersion;
    setContent(newContent);
    setVersion(newVersion);
    pendingOpsRef.current = Math.max(0, pendingOpsRef.current - 1);
    setSaveState(pendingOpsRef.current > 0 ? "saving" : "saved");
    requestAnimationFrame(() => {
      suppressSendRef.current = false;
    });
  }, []);

  useEffect(() => {
    if (!enabled || !documentId) {
      return undefined;
    }

    const token = getToken();
    if (!token) {
      return undefined;
    }

    const client = new Client({
      brokerURL: wsUrl(),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 3000,
      onConnect: () => {
        setConnected(true);

        client.subscribe(`/topic/document/${documentId}`, (message) => {
          const data = JSON.parse(message.body);
          applyRemote(data.content ?? "", data.version);
        });

        client.subscribe("/user/queue/errors", (message) => {
          const data = JSON.parse(message.body);
          applyRemote(data.content ?? "", data.version);
          onConflict?.();
        });
      },
      onDisconnect: () => {
        setConnected(false);
      },
      onStompError: () => {
        setSaveState("error");
      },
      onWebSocketError: () => {
        setConnected(false);
        setSaveState("error");
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
      setConnected(false);
    };
  }, [documentId, enabled, applyRemote, onConflict]);

  const publishOperation = useCallback(
    (operation) => {
      const client = clientRef.current;
      if (!client?.connected || readOnly) {
        return;
      }
      pendingOpsRef.current += 1;
      setSaveState("saving");
      client.publish({
        destination: "/app/document",
        body: JSON.stringify(operation),
      });
    },
    [readOnly]
  );

  const handleContentChange = useCallback(
    (newContent) => {
      if (readOnly) {
        return;
      }

      const oldContent = contentRef.current;
      if (newContent === oldContent) {
        return;
      }

      contentRef.current = newContent;
      setContent(newContent);

      if (
        suppressSendRef.current ||
        !clientRef.current?.connected ||
        versionRef.current === null
      ) {
        return;
      }

      const change = findTextChange(oldContent, newContent);
      let operation = null;

      if (change.deletedText.length > 0) {
        operation = {
          documentId,
          operation: "DELETE",
          position: change.position,
          text: change.deletedText,
          version: versionRef.current,
        };
      } else if (change.insertedText.length > 0) {
        operation = {
          documentId,
          operation: "INSERT",
          position: change.position,
          text: change.insertedText,
          version: versionRef.current,
        };
      }

      if (operation) {
        publishOperation(operation);
      }
    },
    [documentId, readOnly, publishOperation]
  );

  return {
    content,
    version,
    connected,
    saveState,
    setContent: handleContentChange,
    applyRemote,
  };
}
