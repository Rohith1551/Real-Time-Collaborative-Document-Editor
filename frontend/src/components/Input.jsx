export function Input({ label, id, error, className = "", ...props }) {
  const inputId = id ?? props.name;
  return (
    <label className={`field ${className}`.trim()} htmlFor={inputId}>
      {label ? <span className="field-label">{label}</span> : null}
      <input id={inputId} className="field-input" {...props} />
      {error ? <span className="field-error">{error}</span> : null}
    </label>
  );
}
