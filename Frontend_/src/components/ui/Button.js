import React from "react";
import { Loader2 } from "lucide-react";

export default function Button({
  children,
  loading = false,
  variant = "primary",
  type = "button",
  className = "",
  ...rest
}) {
  const variants = {
    primary:
      "bg-brand text-white hover:bg-brand-hover disabled:opacity-60",
    secondary:
      "bg-surface-2 text-text border border-border hover:bg-surface disabled:opacity-60",
    outline:
      "border border-border text-text hover:bg-surface-2 disabled:opacity-60",
    danger: "bg-danger text-white hover:opacity-95 disabled:opacity-60",
    ghost: "text-text-muted hover:bg-surface-2 disabled:opacity-60",
  };
  return (
    <button
      type={type}
      disabled={loading || rest.disabled}
      className={`inline-flex items-center justify-center gap-2 px-4 py-2 text-sm font-semibold rounded-md transition-colors ${variants[variant]} ${className}`}
      {...rest}
    >
      {loading && <Loader2 size={16} className="animate-spin" />}
      {children}
    </button>
  );
}
