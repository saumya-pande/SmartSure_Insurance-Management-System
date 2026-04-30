import React, { useState, useCallback } from "react";
import { Link } from "react-router-dom";
import { useSelector } from "react-redux";
import { selectAuth } from "../../store/slices/authSlice";
import LogoutDialog from "../ui/LogoutDialog";
import Icon from "../ui/Icon";
import ThemeToggleFAB from "../ui/ThemeToggleFAB";

export default function AppNavbar({ onMenu, admin }) {
  const { user } = useSelector(selectAuth);
  const [logoutOpen, setLogoutOpen] = useState(false);

  const openLogout = useCallback(() => setLogoutOpen(true), []);
  const closeLogout = useCallback(() => setLogoutOpen(false), []);

  return (
    <>
      <header className="sticky top-0 z-30 border-b border-border bg-surface/95 backdrop-blur">
        <div className="h-20 px-4 sm:px-6 flex items-center gap-3">
          <Link
            to={admin ? "/admin" : "/app"}
            className="flex items-center font-heading text-3xl sm:text-4xl font-semibold"
            style={{ flexShrink: 0 }}
            aria-label="SmartSure home"
          >
            <span style={{ color: "#1E3A8A" }}>Smart</span>
            <span style={{ color: "#E67E22" }}>Sure</span>
          </Link>

          <button
            type="button"
            onClick={onMenu}
            className="lg:hidden ml-1 p-2 rounded-md text-text-muted hover:bg-surface-2"
            aria-label="Open menu"
          >
            <Icon name="menu" />
          </button>

          {admin && (
            <span className="hidden sm:inline ml-2 px-2 py-0.5 rounded-md text-xs font-semibold bg-brand-soft text-brand">
              ADMIN
            </span>
          )}

          <div className="ml-auto flex items-center gap-2">
            <ThemeToggleFAB floating={false} />
            <div className="hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-md bg-surface-2 text-sm">
              <Icon name="user" className="text-text-muted" />
              <span className="font-medium truncate max-w-[140px]">
                {user?.name || user?.email || "Account"}
              </span>
            </div>
            <button
              type="button"
              onClick={openLogout}
              className="inline-flex items-center gap-2 px-3 py-2 text-sm font-semibold rounded-md border border-border hover:bg-surface-2"
              aria-label="Sign out"
            >
              <Icon name="logout" />
              <span className="hidden sm:inline">Sign out</span>
            </button>
          </div>
        </div>
      </header>
      <LogoutDialog open={logoutOpen} onClose={closeLogout} />
    </>
  );
}
