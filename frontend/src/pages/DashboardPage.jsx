import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ApiError } from "../api/client.js";
import * as documentsApi from "../api/documents.js";
import { useAuth } from "../auth/AuthContext.jsx";
import { Button } from "../components/Button.jsx";
import { ConfirmDialog } from "../components/ConfirmDialog.jsx";
import { EmptyState } from "../components/EmptyState.jsx";
import { Modal } from "../components/Modal.jsx";
import { Input } from "../components/Input.jsx";
import { Spinner } from "../components/Spinner.jsx";

function previewText(content) {
  if (!content) {
    return "Empty document";
  }
  const line = content.replace(/\s+/g, " ").trim();
  return line.length > 120 ? `${line.slice(0, 120)}…` : line;
}

export function DashboardPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [newTitle, setNewTitle] = useState("");
  const [creating, setCreating] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [renameTarget, setRenameTarget] = useState(null);
  const [renameTitle, setRenameTitle] = useState("");
  const [renaming, setRenaming] = useState(false);

  const loadDocuments = useCallback(async () => {
    setError("");
    setLoading(true);
    try {
      const list = await documentsApi.fetchAllDocuments();
      setDocuments(Array.isArray(list) ? list : []);
    } catch (err) {
      setError(
        err instanceof ApiError ? err.message : "Could not load documents."
      );
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadDocuments();
  }, [loadDocuments]);

  const handleCreate = async (event) => {
    event.preventDefault();
    const title = newTitle.trim() || "Untitled document";
    setCreating(true);
    try {
      const doc = await documentsApi.createDocument({ title, content: "" });
      setCreateOpen(false);
      setNewTitle("");
      navigate(`/documents/${doc.id}`);
    } catch (err) {
      setError(
        err instanceof ApiError ? err.message : "Could not create document."
      );
    } finally {
      setCreating(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) {
      return;
    }
    setDeleting(true);
    try {
      await documentsApi.deleteDocument(deleteTarget.id);
      setDeleteTarget(null);
      await loadDocuments();
    } catch (err) {
      setError(
        err instanceof ApiError ? err.message : "Only the owner can delete."
      );
      setDeleteTarget(null);
    } finally {
      setDeleting(false);
    }
  };

  const handleRename = async (event) => {
    event.preventDefault();
    if (!renameTarget) {
      return;
    }
    const title = renameTitle.trim() || "Untitled document";
    setRenaming(true);
    try {
      await documentsApi.updateDocument(renameTarget.id, {
        title,
        content: renameTarget.content ?? "",
        version: renameTarget.version,
      });
      setRenameTarget(null);
      await loadDocuments();
    } catch (err) {
      setError(
        err instanceof ApiError ? err.message : "Could not rename document."
      );
    } finally {
      setRenaming(false);
    }
  };

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <div>
          <Link to="/" className="brand">
            CollabDocs
          </Link>
          <p className="dashboard-greeting">Signed in as {user?.email}</p>
        </div>
        <div className="dashboard-header-actions">
          <Button onClick={() => setCreateOpen(true)}>New document</Button>
          <Button variant="secondary" onClick={logout}>
            Sign out
          </Button>
        </div>
      </header>

      <main className="dashboard-main">
        <h1>Your documents</h1>
        {error ? (
          <p className="banner banner-error" role="alert">
            {error}
          </p>
        ) : null}

        {loading ? (
          <Spinner label="Loading documents…" />
        ) : documents.length === 0 ? (
          <EmptyState
            title="No documents yet"
            description="Create your first document to start collaborating in real time."
            actionLabel="New document"
            onAction={() => setCreateOpen(true)}
          />
        ) : (
          <ul className="doc-grid">
            {documents.map((doc) => (
              <li key={doc.id} className="doc-card">
                <Link to={`/documents/${doc.id}`} className="doc-card-link">
                  <h2>{doc.title || "Untitled document"}</h2>
                  <p>{previewText(doc.content)}</p>
                  <span className="doc-meta">Version {doc.version ?? 0}</span>
                </Link>
                <div className="doc-card-actions">
                  <Button
                    variant="ghost"
                    onClick={() => {
                      setRenameTarget(doc);
                      setRenameTitle(doc.title || "");
                    }}
                  >
                    Rename
                  </Button>
                  <Button
                    variant="ghost"
                    className="danger-text"
                    onClick={() => setDeleteTarget(doc)}
                  >
                    Delete
                  </Button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </main>

      <Modal
        open={createOpen}
        title="New document"
        onClose={() => !creating && setCreateOpen(false)}
        footer={
          <>
            <Button
              variant="secondary"
              onClick={() => setCreateOpen(false)}
              disabled={creating}
            >
              Cancel
            </Button>
            <Button type="submit" form="create-doc-form" disabled={creating}>
              {creating ? "Creating…" : "Create"}
            </Button>
          </>
        }
      >
        <form id="create-doc-form" onSubmit={handleCreate}>
          <Input
            label="Title"
            name="title"
            value={newTitle}
            onChange={(e) => setNewTitle(e.target.value)}
            placeholder="Untitled document"
            autoFocus
          />
        </form>
      </Modal>

      <Modal
        open={Boolean(renameTarget)}
        title="Rename document"
        onClose={() => !renaming && setRenameTarget(null)}
        footer={
          <>
            <Button
              variant="secondary"
              onClick={() => setRenameTarget(null)}
              disabled={renaming}
            >
              Cancel
            </Button>
            <Button type="submit" form="rename-doc-form" disabled={renaming}>
              {renaming ? "Saving…" : "Save"}
            </Button>
          </>
        }
      >
        <form id="rename-doc-form" onSubmit={handleRename}>
          <Input
            label="Title"
            name="rename"
            value={renameTitle}
            onChange={(e) => setRenameTitle(e.target.value)}
            autoFocus
          />
        </form>
      </Modal>

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        title="Delete document?"
        message={`"${deleteTarget?.title || "Untitled document"}" will be permanently removed. This cannot be undone.`}
        confirmLabel={deleting ? "Deleting…" : "Delete"}
        destructive
        busy={deleting}
        onCancel={() => !deleting && setDeleteTarget(null)}
        onConfirm={handleDelete}
      />
    </div>
  );
}
