import React from "react";
import { Link, NavLink, Outlet, useLocation } from "react-router-dom";
import { useSelector } from "react-redux";
import { selectAuth } from "../store/slices/authSlice";
import ThemeToggleFAB from "../components/ui/ThemeToggleFAB";

export default function PublicLayout() {
  const { token, user } = useSelector(selectAuth);
  const { pathname } = useLocation();
  const homeHref = token ? (user?.role === "ADMIN" ? "/admin" : "/app") : "/";

  return (
    <div className="min-h-screen flex flex-col bg-bg text-text">
      <header className="border-b border-border bg-surface">
        <div className="h-20 px-3 sm:px-4 lg:px-5 flex items-center gap-4">
          <Link
            to={homeHref}
            className="flex items-center font-heading text-3xl sm:text-4xl font-semibold"
            style={{ flexShrink: 0 }}
            aria-label="SmartSure home"
          >
            <span style={{ color: "#1E3A8A" }}>Smart</span>
            <span style={{ color: "#E67E22" }}>Sure</span>
          </Link>

          <nav className="ml-2 hidden sm:flex items-center gap-1">
            <NavLink
              to="/policies"
              className={({ isActive }) =>
                `px-3 py-2 text-sm rounded-md ${
                  isActive ? "text-brand font-semibold bg-brand-soft" : "text-text-muted hover:bg-surface-2"
                }`
              }
            >
              Policies
            </NavLink>
          </nav>

          <div className="ml-auto flex items-center gap-2">
            <NavLink
              to="/policies"
              className={({ isActive }) =>
                `sm:hidden px-3 py-2 text-sm rounded-md ${
                  isActive ? "text-brand font-semibold bg-brand-soft" : "text-text-muted hover:bg-surface-2"
                }`
              }
            >
              Policies
            </NavLink>
            <ThemeToggleFAB floating={false} />
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
        <footer className="border-t border-border bg-[#07111f] text-white">
          <div className="mx-auto max-w-[1760px] px-5 sm:px-10 lg:px-20 py-14">
            <div className="grid gap-10 lg:grid-cols-[1.2fr_2fr_1.1fr]">
              <div>
                <Link to={homeHref} className="font-heading text-3xl font-semibold">
                  <span className="text-[#9db8ff]">Smart</span>
                  <span className="text-[#f39b3d]">Sure</span>
                </Link>
                <p className="mt-4 max-w-sm text-sm leading-6 text-slate-300">
                  Insurance platform for clear policy buying, KYC, claims tracking, and customer support.
                </p>
                <p className="mt-6 text-xs uppercase tracking-widest text-slate-500">
                  © {new Date().getFullYear()} SmartSure Insurance Services
                </p>
              </div>
              <div className="grid grid-cols-2 gap-8 sm:grid-cols-4">
                {[
                  ["About", "Company", "Careers", "Security", "Partners"],
                  ["Policies", "Vehicle", "Home"],
                  ["Claims", "How claims work", "Track claim", "Documents", "Settlement FAQ"],
                  ["Support", "Contact", "Help center", "Feedback", "Terms"],
                ].map(([title, ...links]) => (
                  <div key={title}>
                    <h3 className="text-sm font-semibold uppercase tracking-widest text-slate-400">{title}</h3>
                    <div className="mt-4 space-y-3 text-sm text-slate-200">
                      {links.map((item) => (
                        <a key={item} href="/" className="block hover:text-white">
                          {item}
                        </a>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
              <form className="rounded-lg border border-white/10 bg-white/5 p-5">
                <h3 className="font-heading text-2xl font-semibold">Feedback</h3>
                <input
                  className="mt-4 w-full rounded-md border border-white/10 bg-white/10 px-3 py-2 text-sm text-white placeholder:text-slate-400"
                  placeholder="Email"
                />
                <textarea
                  className="mt-3 h-24 w-full rounded-md border border-white/10 bg-white/10 px-3 py-2 text-sm text-white placeholder:text-slate-400"
                  placeholder="Tell us what to improve"
                />
                <button className="mt-3 w-full rounded-md bg-white px-4 py-2 text-sm font-semibold text-[#07111f]">
                  Send feedback
                </button>
              </form>
            </div>
          </div>
        </footer>
      )}
    </div>
  );
}
