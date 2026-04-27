import React from "react";

export default function EmptyState({ icon: Icon, title, description, action }) {
  return (
    <div className="rounded-2xl border border-dashed border-border bg-surface p-10 text-center">
      {Icon && (
        <div className="mx-auto w-12 h-12 rounded-full bg-surface-2 grid place-items-center text-text-muted mb-3">
          <Icon size={22} />
        </div>
      )}
      <h3 className="font-heading text-lg font-semibold">{title}</h3>
      {description && (
        <p className="mt-1 text-sm text-text-muted max-w-md mx-auto">{description}</p>
      )}
      {action && <div className="mt-5">{action}</div>}
    </div>
  );
}
