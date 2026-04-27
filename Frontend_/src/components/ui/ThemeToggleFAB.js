import React, { useEffect, useState, useCallback } from "react";
import { Moon, Sun } from "lucide-react";

const KEY = "smartsure_theme";

export default function ThemeToggleFAB() {
  // Default LIGHT — no OS preference detection (rule #9)
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

  const toggle = useCallback(() => setDark((d) => !d), []);

  return (
    <button
      type="button"
      onClick={toggle}
      aria-label={dark ? "Switch to light mode" : "Switch to dark mode"}
      className="fixed z-50 bottom-5 right-5 w-12 h-12 rounded-full bg-surface border border-border shadow-elevated grid place-items-center text-text hover:bg-surface-2 transition-colors"
    >
      {dark ? <Sun size={18} /> : <Moon size={18} />}
    </button>
  );
}
