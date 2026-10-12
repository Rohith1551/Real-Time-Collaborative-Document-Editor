export const API_BASE = import.meta.env.VITE_API_BASE ?? "";

export function wsUrl() {
  const configured = import.meta.env.VITE_WS_URL;
  if (configured) {
    return configured;
  }
  const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
  const host = import.meta.env.DEV
    ? `${window.location.hostname}:8080`
    : window.location.host;
  return `${protocol}//${host}/ws`;
}
