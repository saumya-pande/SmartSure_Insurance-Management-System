import React, { useEffect, useState, useCallback } from "react";
import Icon from "./Icon";

const KEY = "smartsure_theme";

export default function ThemeToggleFAB({ floating = true, className = "" }) {
  const [dark, setDark] = useState(() => {
    try {
      return localStorage.getItem(KEY) === "dark";
    } catch {
      return false;
    }
  });

  useEffect(() => {
    const html = document.documentElement;
    if (dark) html.classList.add("dark");
    else html.classList.remove("dark");
    try {
      localStorage.setItem(KEY, dark ? "dark" : "light");
    } catch {
      /* noop */
    }
  }, [dark]);

  const toggle = useCallback(() => setDark((value) => !value), []);

  const classes = floating
    ? "fixed z-50 bottom-5 right-5 w-12 h-12 rounded-full bg-surface border border-border shadow-elevated grid place-items-center text-text hover:bg-surface-2 transition-colors"
    : "inline-flex items-center justify-center w-10 h-10 rounded-full border border-border bg-surface text-text hover:bg-surface-2 transition-colors";

  return (
    <button
      type="button"
      onClick={toggle}
      aria-label={dark ? "Switch to light mode" : "Switch to dark mode"}
      className={`${classes} ${className}`.trim()}
    >
      {dark ? <Icon name="sun" /> : <Icon name="moon" />}
    </button>
  );
}
