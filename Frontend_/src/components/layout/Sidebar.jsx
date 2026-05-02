import React, { useEffect, useCallback } from "react";
import { NavLink } from "react-router-dom";
import Icon from "../ui/Icon";

export default function Sidebar({ items, open, onClose, title = "Navigation" }) {
  // Close on Escape
  useEffect(() => {
    if (!open) return undefined;
    const onKey = (e) => {
      if (e.key === "Escape") onClose();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open, onClose]);

  // Close on nav item click (mobile)
  const handleNavClick = useCallback(() => {
    if (open) onClose();
  }, [open, onClose]);

  // Group items
  const groups = items.reduce((acc, item) => {
    const g = item.group || "MAIN";
    if (!acc[g]) acc[g] = [];
    acc[g].push(item);
    return acc;
  }, {});

  const content = (
    <nav className="h-full overflow-y-auto py-6 px-3">
      {Object.entries(groups).map(([group, list]) => (
        <div key={group} className="mb-6">
          <p className="px-3 mb-2 text-[11px] font-semibold tracking-widest text-text-subtle">
            {group}
          </p>
          <ul className="space-y-1">
            {list.map((item) => {
              const ItemIcon = item.icon;
              return (
                <li key={item.to}>
                  <NavLink
                    to={item.to}
                    end={item.end}
                    onClick={handleNavClick}
                    className={({ isActive }) =>
                      `flex items-center gap-3 px-3 py-2.5 rounded-md text-base transition-colors ${
                        isActive
                          ? "bg-brand-soft text-brand font-semibold"
                          : "text-text-muted hover:bg-surface-2 hover:text-text"
                      }`
                    }
                  >
                    {ItemIcon ? <ItemIcon size={20} /> : null}
                    <span>{item.label}</span>
                  </NavLink>
                </li>
              );
            })}
          </ul>
        </div>
      ))}
    </nav>
  );

  return (
    <>
      {/* Desktop */}
      <aside className="hidden lg:block h-full w-64 shrink-0 border-r border-border bg-surface overflow-y-auto">
        {content}
      </aside>

      {/* Mobile drawer */}
      {open && (
        <div className="lg:hidden fixed inset-0 z-40">
          <div
            className="absolute inset-0 bg-black/40"
            onClick={onClose}
            aria-hidden="true"
          />
          <aside
            className="relative h-full w-[min(20rem,88vw)] bg-surface border-r border-border shadow-elevated flex flex-col"
            role="dialog"
            aria-modal="true"
            aria-label={title}
          >
            <div className="flex items-center justify-between px-4 py-4 border-b border-border">
              <p className="text-sm font-semibold">{title}</p>
              <button
                type="button"
                onClick={onClose}
                className="inline-flex items-center justify-center w-10 h-10 rounded-full border border-border hover:bg-surface-2"
                aria-label="Close navigation"
              >
                <Icon name="close" />
              </button>
            </div>
            <div className="flex-1 min-h-0">{content}</div>
          </aside>
        </div>
      )}
    </>
  );
}
