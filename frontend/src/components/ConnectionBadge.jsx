export function ConnectionBadge({ connected }) {
  return (
    <span
      className={`badge ${connected ? "badge-ok" : "badge-warn"}`}
      title={connected ? "Real-time connection active" : "Reconnecting…"}
    >
      <span className="badge-dot" aria-hidden="true" />
      {connected ? "Live" : "Offline"}
    </span>
  );
}
