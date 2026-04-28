import React from "react";
import { NavLink } from "react-router-dom";

export default function MobileBottomNav({ items }) {
  return (
    <nav
      className="lg:hidden fixed inset-x-0 bottom-0 z-30 border-t border-border bg-surface/95 backdrop-blur"
      aria-label="Primary navigation"
      style={{ paddingBottom: "max(env(safe-area-inset-bottom), 0.5rem)" }}
    >
      <ul
        className="grid gap-1 px-2 pt-2"
        style={{ gridTemplateColumns: `repeat(${Math.max(items.length, 1)}, minmax(0, 1fr))` }}
      >
        {items.map((item) => {
          const Icon = item.icon;
          return (
            <li key={item.to}>
              <NavLink
                to={item.to}
                end={item.end}
                className={({ isActive }) =>
                  [
                    "flex flex-col items-center justify-center gap-1 rounded-2xl px-2 py-2.5 text-xs font-medium transition-colors min-h-[64px]",
                    isActive
                      ? "bg-brand-soft text-brand"
                      : "text-text-muted hover:bg-surface-2 hover:text-text",
                  ].join(" ")
                }
              >
                {Icon ? <Icon size={18} /> : null}
                <span>{item.label}</span>
              </NavLink>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}
