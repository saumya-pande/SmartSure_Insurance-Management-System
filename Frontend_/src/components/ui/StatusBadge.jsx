import React from "react";
import { statusVariant } from "../../lib/constants";

const STYLES = {
  success: "bg-success-soft text-success border-success/20",
  warning: "bg-warning-soft text-warning border-warning/20",
  danger: "bg-danger-soft text-danger border-danger/20",
  info: "bg-info-soft text-info border-info/20",
  neutral: "bg-surface-2 text-text-muted border-border",
};

export default function StatusBadge({ status, children }) {
  const v = statusVariant(status);
  const label = children || String(status || "—").replace(/_/g, " ");
  return (
    <span
      className={`inline-flex items-center px-2 py-0.5 rounded-md border text-xs font-semibold tracking-wide uppercase ${STYLES[v]}`}
    >
      {label}
    </span>
  );
}
