const LABELS = {
  saved: "All changes saved",
  saving: "Saving…",
  error: "Sync error — edits may be out of date",
};

export function SaveStatus({ state }) {
  return (
    <span className={`save-status save-status-${state}`} aria-live="polite">
      {LABELS[state] ?? LABELS.saved}
    </span>
  );
}
