import React, { forwardRef, useState } from "react";

const Field = forwardRef(function Field(
  { label, id, error, hint, type = "text", className = "", as = "input", children, ...rest },
  ref
) {
  const Cmp = as;
  const isPassword = type === "password";
  const [showPassword, setShowPassword] = useState(false);

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
        <div className={isPassword ? "relative" : undefined}>
          <Cmp
            id={id}
            ref={ref}
            type={as === "input" ? (isPassword ? (showPassword ? "text" : "password") : type) : undefined}
            rows={as === "textarea" ? rest.rows || 4 : undefined}
            className={`w-full px-3 py-2 rounded-md border border-border bg-surface text-text placeholder:text-text-subtle focus:border-brand focus:outline-none${isPassword ? " pr-10" : ""}`}
            {...rest}
          />
          {isPassword && (
            <button
              type="button"
              tabIndex={-1}
              onClick={() => setShowPassword((prev) => !prev)}
              className="absolute right-2 top-1/2 -translate-y-1/2 p-1 text-text-subtle hover:text-text transition-colors"
              aria-label={showPassword ? "Hide password" : "Show password"}
            >
              <i
                aria-hidden="true"
                className={`fa-solid ${showPassword ? "fa-eye-slash" : "fa-eye"} text-sm`}
              />
            </button>
          )}
        </div>
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
