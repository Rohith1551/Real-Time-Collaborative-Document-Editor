import { useCallback, useEffect, useRef, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ApiError } from "../api/client.js";
import * as documentsApi from "../api/documents.js";
import { ConnectionBadge } from "../components/ConnectionBadge.jsx";
import { Button } from "../components/Button.jsx";
import { ConfirmDialog } from "../components/ConfirmDialog.jsx";
import { Modal } from "../components/Modal.jsx";
import { Input } from "../components/Input.jsx";
import { SaveStatus } from "../components/SaveStatus.jsx";
import { Spinner } from "../components/Spinner.jsx";
import { useDocumentCollaboration } from "../hooks/useDocumentCollaboration.js";

const ROLES = ["EDITOR", "VIEWER"];

export function EditorPage() {
  const { id } = useParams();
  const documentId = Number(id);
  const navigate = useNavigate();
  const titleSaveTimer = useRef(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [title, setTitle] = useState("");
  const [permission, setPermission] = useState(null);
  const [conflictNotice, setConflictNotice] = useState("");
  const [shareOpen, setShareOpen] = useState(false);
  const [shareEmail, setShareEmail] = useState("");
  const [shareRole, setShareRole] = useState("EDITOR");
  const [shareError, setShareError] = useState("");
  const [sharing, setSharing] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [versionsOpen, setVersionsOpen] = useState(false);
  const [versions, setVersions] = useState([]);
  const [versionsLoading, setVersionsLoading] = useState(false);

  const readOnly =
    permission?.role === "VIEWER" ||
    (permission?.role !== "OWNER" && permission?.role !== "EDITOR");

  const [docMeta, setDocMeta] = useState(null);

  const onConflict = useCallback(() => {
    setConflictNotice(
      "Your edit conflicted with another change. The document was synchronized to the latest version."
    );
  }, []);

  const {
    content,
    version,
    connected,
    saveState,
    setContent,
    applyRemote,
  } = useDocumentCollaboration({
    documentId,
    enabled: Boolean(docMeta) && !Number.isNaN(documentId),
    readOnly,
    initialContent: docMeta?.content ?? "",
    initialVersion: docMeta?.version ?? null,
    onConflict,
  });

  useEffect(() => {
    if (Number.isNaN(documentId)) {
      setError("Invalid document id.");
      setLoading(false);
      return;
    }

    let cancelled = false;

    (async () => {
      setLoading(true);
      setError("");
      try {
        const [doc, perm] = await Promise.all([
          documentsApi.fetchDocument(documentId),
          documentsApi.fetchPermission(documentId),
        ]);
        if (cancelled) {
          return;
        }
        setDocMeta(doc);
        setTitle(doc.title ?? "");
        setPermission(perm);
      } catch (err) {
        if (!cancelled) {
          setError(
            err instanceof ApiError
              ? err.message
              : "Unable to open this document."
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [documentId]);

  const persistTitle = useCallback(
    (nextTitle) => {
      if (readOnly || !docMeta) {
        return;
      }
      documentsApi
        .updateDocument(documentId, {
          title: nextTitle,
          content,
          version,
        })
        .catch(() => {
          setConflictNotice("Could not save title. Refresh and try again.");
        });
    },
    [readOnly, docMeta, documentId, content, version]
  );

  const handleTitleChange = (event) => {
    const next = event.target.value;
    setTitle(next);
    if (readOnly) {
      return;
    }
    clearTimeout(titleSaveTimer.current);
    titleSaveTimer.current = setTimeout(() => {
      persistTitle(next.trim() || "Untitled document");
    }, 600);
  };

  const handleShare = async (event) => {
    event.preventDefault();
    setShareError("");
    setSharing(true);
    try {
      await documentsApi.shareDocument(
        documentId,
        shareEmail.trim(),
        shareRole
      );
      setShareOpen(false);
      setShareEmail("");
    } catch (err) {
      setShareError(
        err instanceof ApiError ? err.message : "Could not share document."
      );
    } finally {
      setSharing(false);
    }
  };

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await documentsApi.deleteDocument(documentId);
      navigate("/documents", { replace: true });
    } catch (err) {
      setError(
        err instanceof ApiError ? err.message : "Only the owner can delete."
      );
      setDeleteOpen(false);
    } finally {
      setDeleting(false);
    }
  };

  const openVersions = async () => {
    setVersionsOpen(true);
    setVersionsLoading(true);
    try {
      const list = await documentsApi.fetchVersions(documentId);
      setVersions(Array.isArray(list) ? list : []);
    } catch (err) {
      setError(
        err instanceof ApiError ? err.message : "Could not load versions."
      );
    } finally {
      setVersionsLoading(false);
    }
  };

  const handleRestore = async (versionId) => {
    try {
      const restored = await documentsApi.restoreVersion(
        documentId,
        versionId
      );
      applyRemote(restored.content ?? "", restored.version);
      setTitle(restored.title ?? title);
      setDocMeta(restored);
      setVersionsOpen(false);
    } catch (err) {
      setError(
        err instanceof ApiError ? err.message : "Could not restore version."
      );
    }
  };

  if (loading) {
    return (
      <div className="editor-shell">
        <Spinner label="Opening document…" />
      </div>
    );
  }

  if (error && !docMeta) {
    return (
      <div className="editor-shell editor-error">
        <p className="banner banner-error">{error}</p>
        <Link to="/documents">
          <Button variant="secondary">Back to documents</Button>
        </Link>
      </div>
    );
  }

  const canShare = permission?.role === "OWNER";
  const canDelete = permission?.role === "OWNER";

  return (
    <div className="editor-shell">
      <header className="editor-toolbar">
        <div className="editor-toolbar-left">
          <Link to="/documents" className="btn btn-ghost back-link">
            ← Documents
          </Link>
          <input
            className="title-input"
            value={title}
            onChange={handleTitleChange}
            readOnly={readOnly}
            placeholder="Untitled document"
            aria-label="Document title"
          />
        </div>
        <div className="editor-toolbar-center">
          <ConnectionBadge connected={connected} />
          <SaveStatus state={readOnly ? "saved" : saveState} />
          {readOnly ? (
            <span className="role-pill">View only</span>
          ) : null}
        </div>
        <div className="editor-toolbar-right">
          <Button variant="secondary" onClick={openVersions}>
            History
          </Button>
          {canShare ? (
            <Button variant="secondary" onClick={() => setShareOpen(true)}>
              Share
            </Button>
          ) : null}
          {canDelete ? (
            <Button variant="danger" onClick={() => setDeleteOpen(true)}>
              Delete
            </Button>
          ) : null}
        </div>
      </header>

      {conflictNotice ? (
        <p className="banner banner-warn" role="status">
          {conflictNotice}
          <button
            type="button"
            className="banner-dismiss"
            onClick={() => setConflictNotice("")}
          >
            Dismiss
          </button>
        </p>
      ) : null}

      {error ? (
        <p className="banner banner-error" role="alert">
          {error}
        </p>
      ) : null}

      <div className="editor-body">
        <textarea
          className="editor-area"
          value={content}
          onChange={(e) => setContent(e.target.value)}
          readOnly={readOnly || !connected}
          placeholder={
            readOnly
              ? "You have view-only access to this document."
              : connected
                ? "Start writing…"
                : "Connecting to live session…"
          }
          spellCheck
        />
        <aside className="editor-sidebar">
          <h3>Document info</h3>
          <dl className="meta-list">
            <div>
              <dt>Version</dt>
              <dd>{version ?? "—"}</dd>
            </div>
            <div>
              <dt>Your role</dt>
              <dd>{permission?.role ?? "—"}</dd>
            </div>
            <div>
              <dt>Words</dt>
              <dd>
                {
                  content.trim()
                    ? content.trim().split(/\s+/).length
                    : 0
                }
              </dd>
            </div>
          </dl>
          <p className="sidebar-hint">
            Edits sync in real time over WebSockets. Plain text is supported;
            rich formatting is not stored by the backend yet.
          </p>
        </aside>
      </div>

      <Modal
        open={shareOpen}
        title="Share document"
        onClose={() => !sharing && setShareOpen(false)}
        footer={
          <>
            <Button
              variant="secondary"
              onClick={() => setShareOpen(false)}
              disabled={sharing}
            >
              Cancel
            </Button>
            <Button type="submit" form="share-form" disabled={sharing}>
              {sharing ? "Sharing…" : "Share"}
            </Button>
          </>
        }
      >
        <form id="share-form" className="stack-form" onSubmit={handleShare}>
          <Input
            label="Collaborator email"
            type="email"
            required
            value={shareEmail}
            onChange={(e) => setShareEmail(e.target.value)}
          />
          <label className="field">
            <span className="field-label">Role</span>
            <select
              className="field-input"
              value={shareRole}
              onChange={(e) => setShareRole(e.target.value)}
            >
              {ROLES.map((role) => (
                <option key={role} value={role}>
                  {role}
                </option>
              ))}
            </select>
          </label>
          {shareError ? <p className="form-error">{shareError}</p> : null}
        </form>
      </Modal>

      <Modal
        open={versionsOpen}
        title="Version history"
        onClose={() => setVersionsOpen(false)}
      >
        {versionsLoading ? (
          <Spinner label="Loading versions…" />
        ) : versions.length === 0 ? (
          <p>No saved versions yet. Snapshots are created when you rename or save via REST.</p>
        ) : (
          <ul className="version-list">
            {versions.map((v) => (
              <li key={v.id}>
                <div>
                  <strong>{v.title || "Untitled"}</strong>
                  <span>{v.createdAt ? new Date(v.createdAt).toLocaleString() : ""}</span>
                </div>
                {permission?.role === "OWNER" || permission?.role === "EDITOR" ? (
                  <Button variant="secondary" onClick={() => handleRestore(v.id)}>
                    Restore
                  </Button>
                ) : null}
              </li>
            ))}
          </ul>
        )}
      </Modal>

      <ConfirmDialog
        open={deleteOpen}
        title="Delete this document?"
        message="This permanently removes the document for everyone."
        destructive
        busy={deleting}
        onCancel={() => !deleting && setDeleteOpen(false)}
        onConfirm={handleDelete}
      />
    </div>
  );
}
