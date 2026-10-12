import { apiFetch } from "./client.js";

export function fetchAllDocuments() {
  return apiFetch("/api/documents/getAllDocuments");
}

export function fetchDocument(id) {
  return apiFetch(`/api/documents/getDocument/${id}`);
}

export function createDocument({ title, content = "" }) {
  return apiFetch("/api/documents/createDocument", {
    method: "POST",
    body: JSON.stringify({ title, content }),
  });
}

export function updateDocument(id, payload) {
  return apiFetch(`/api/documents/updateDocument/${id}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export function deleteDocument(id) {
  return apiFetch(`/api/documents/deleteDocument/${id}`, {
    method: "DELETE",
  });
}

export function fetchPermission(documentId) {
  return apiFetch(`/api/documents/getPermission/${documentId}`, {
    method: "POST",
  });
}

export function shareDocument(documentId, email, role) {
  const params = new URLSearchParams({ email, role });
  return apiFetch(
    `/api/documents/sharePermission/${documentId}?${params.toString()}`,
    { method: "POST" }
  );
}

export function fetchVersions(documentId) {
  return apiFetch(`/api/documents/getVersion/${documentId}`);
}

export function restoreVersion(documentId, versionId) {
  return apiFetch(
    `/api/documents/${documentId}/versions/${versionId}/restore`,
    { method: "PUT" }
  );
}
