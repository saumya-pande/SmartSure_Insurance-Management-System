import React from "react";
import { Link, NavLink, Outlet, useLocation } from "react-router-dom";
import { useSelector } from "react-redux";
import { selectAuth } from "../store/slices/authSlice";
import Icon from "../components/ui/Icon";

export default function PublicLayout() {
  const { token, user } = useSelector(selectAuth);
  const { pathname } = useLocation();
  const homeHref = token ? (user?.role === "ADMIN" ? "/admin" : "/app") : "/";

  return (
    <div className="min-h-screen flex flex-col bg-bg text-text">
      <header className="border-b border-border bg-surface">
        <div className="max-w-7xl mx-auto h-16 px-4 sm:px-6 flex items-center gap-6">
          <Link
            to={homeHref}
            className="flex items-center gap-2 font-heading text-xl font-semibold"
            style={{ flexShrink: 0 }}
            aria-label="SmartSure home"
          >
            <span className="grid place-items-center w-8 h-8 rounded-md bg-brand text-white">
              <Icon name="shield" />
            </span>
            SmartSure
          </Link>

          <nav className="hidden sm:flex items-center gap-1 ml-2">
            <NavItem to="/" end>
              Home
            </NavItem>
          </nav>

          <div className="ml-auto flex items-center gap-2">
            {!token ? (
              <>
                <NavLink
                  to="/login"
                  className={({ isActive }) =>
                    `px-3 py-2 text-sm rounded-md hover:bg-surface-2 ${
                      isActive ? "text-brand font-semibold" : "text-text-muted"
                    }`
                  }
                >
                  Sign in
                </NavLink>
                <Link
                  to="/register"
                  className="px-4 py-2 text-sm font-semibold rounded-md bg-brand text-white hover:bg-brand-hover transition-colors"
                >
                  Get started
                </Link>
              </>
            ) : (
              <Link
                to={homeHref}
                className="px-4 py-2 text-sm font-semibold rounded-md bg-brand text-white hover:bg-brand-hover transition-colors"
              >
                Open dashboard
              </Link>
            )}
          </div>
        </div>
      </header>

      <main className="flex-1">
        <Outlet />
      </main>

      {pathname === "/" && (
        <footer className="border-t border-border bg-surface">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 py-8 text-sm text-text-muted flex flex-wrap items-center justify-between gap-4">
            <span>© {new Date().getFullYear()} SmartSure. All rights reserved.</span>
            <span className="font-heading">Coverage. Confidence. Clarity.</span>
          </div>
        </footer>
      )}
    </div>
  );
}

function NavItem({ to, end, children }) {
  return (
    <NavLink
      to={to}
      end={end}
      className={({ isActive }) =>
        `px-3 py-2 text-sm rounded-md ${
          isActive
            ? "text-brand font-semibold bg-brand-soft"
            : "text-text-muted hover:bg-surface-2"
        }`
      }
    >
      {children}
    </NavLink>
  );
}
