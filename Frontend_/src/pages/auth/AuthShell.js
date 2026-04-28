import React from "react";
import { Link } from "react-router-dom";
import Icon from "../../components/ui/Icon";

export default function AuthShell({ title, subtitle, children, footer }) {
  return (
    <div className="min-h-[calc(100vh-4rem)] grid lg:grid-cols-2">
      <div className="hidden lg:flex relative overflow-hidden bg-gradient-to-br from-brand to-[#0B1220] text-white p-12 flex-col justify-between">
        <Link to="/" className="flex items-center gap-2 font-heading text-2xl font-semibold">
          <span className="grid place-items-center w-9 h-9 rounded-md bg-white/15">
            <Icon name="shield" />
          </span>
          SmartSure
        </Link>
        <div>
          <p className="font-heading text-4xl leading-tight">
            Insurance should not feel like maze.
            <br />
            SmartSure makes path clear.
          </p>
          <p className="mt-4 text-white/70 text-sm">Verified access. Calm operations. Clear next step.</p>
        </div>
        <div className="absolute -top-32 -right-32 w-96 h-96 rounded-full bg-accent/20 blur-3xl" />
      </div>
      <div className="flex items-center justify-center px-4 sm:px-8 py-10">
        <div className="w-full max-w-md">
          <h1 className="font-heading text-3xl sm:text-4xl font-semibold">{title}</h1>
          {subtitle && <p className="mt-2 text-text-muted">{subtitle}</p>}
          <div className="mt-8">{children}</div>
          {footer && <div className="mt-6 text-sm text-text-muted">{footer}</div>}
        </div>
      </div>
    </div>
  );
}
