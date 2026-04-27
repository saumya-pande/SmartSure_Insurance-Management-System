import React, { useState, useCallback } from "react";
import { Link } from "react-router-dom";
import { useSelector } from "react-redux";
import { Menu, Shield, LogOut, User } from "lucide-react";
import { selectAuth } from "../../store/slices/authSlice";
import LogoutDialog from "../ui/LogoutDialog";

export default function AppNavbar({ onMenu, admin }) {
  const { user } = useSelector(selectAuth);
  const [logoutOpen, setLogoutOpen] = useState(false);

  const openLogout = useCallback(() => setLogoutOpen(true), []);
  const closeLogout = useCallback(() => setLogoutOpen(false), []);

  return (
    <>
      <header className="sticky top-0 z-30 border-b border-border bg-surface/95 backdrop-blur">
        <div className="h-16 px-4 sm:px-6 flex items-center gap-3">
          <Link
            to={admin ? "/admin" : "/app"}
            className="flex items-center gap-2 font-heading text-xl font-semibold"
            style={{ flexShrink: 0 }}
            aria-label="SmartSure home"
          >
            <span className="grid place-items-center w-8 h-8 rounded-md bg-brand text-white">
              <Shield size={18} />
            </span>
            <span className="hidden xs:inline sm:inline">SmartSure</span>
          </Link>

          <button
            type="button"
            onClick={onMenu}
            className="lg:hidden ml-1 p-2 rounded-md text-text-muted hover:bg-surface-2"
            aria-label="Open menu"
          >
            <Menu size={20} />
          </button>

          {admin && (
            <span className="hidden sm:inline ml-2 px-2 py-0.5 rounded-md text-xs font-semibold bg-brand-soft text-brand">
              ADMIN
            </span>
          )}

          <div className="ml-auto flex items-center gap-2">
            <div className="hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-md bg-surface-2 text-sm">
              <User size={14} className="text-text-muted" />
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
              <LogOut size={16} />
              <span className="hidden sm:inline">Sign out</span>
            </button>
          </div>
        </div>
      </header>
      <LogoutDialog open={logoutOpen} onClose={closeLogout} />
    </>
  );
}
