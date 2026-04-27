import React, { forwardRef } from "react";

const Field = forwardRef(function Field(
  { label, id, error, hint, type = "text", className = "", as = "input", children, ...rest },
  ref
) {
  const Cmp = as;
  return (
    <div className={className}>
      {label && (
        <label htmlFor={id} className="block text-sm font-semibold text-text mb-1.5">
          {label}
        </label>
      )}
      {as === "select" ? (
        <Cmp
          id={id}
          ref={ref}
          className="w-full px-3 py-2 rounded-md border border-border bg-surface text-text focus:border-brand focus:outline-none"
          {...rest}
        >
          {children}
        </Cmp>
      ) : (
        <Cmp
          id={id}
          ref={ref}
          type={as === "input" ? type : undefined}
          rows={as === "textarea" ? rest.rows || 4 : undefined}
          className="w-full px-3 py-2 rounded-md border border-border bg-surface text-text placeholder:text-text-subtle focus:border-brand focus:outline-none"
          {...rest}
        />
      )}
      {hint && !error && <p className="mt-1 text-xs text-text-subtle">{hint}</p>}
      {error && (
        <p className="mt-1 text-xs text-danger" role="alert">
          {error}
        </p>
      )}
    </div>
  );
});

export default Field;
