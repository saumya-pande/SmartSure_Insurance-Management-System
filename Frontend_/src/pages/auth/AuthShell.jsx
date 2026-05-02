import React from "react";

export default function AuthShell({ title, subtitle, children, footer }) {
  return (
    <div className="min-h-[calc(100vh-4rem)] grid lg:grid-cols-2">
      <div className="hidden lg:flex relative overflow-hidden bg-gradient-to-br from-brand to-[#0B1220] text-white p-12 flex-col justify-end">
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
      <div className="flex items-center justify-center px-4 sm:px-6 py-8">
        <div className="w-full max-w-md">
          <h1 className="font-heading text-3xl sm:text-4xl font-semibold">{title}</h1>
          {subtitle && <p className="mt-2 text-text-muted">{subtitle}</p>}
          <div className="mt-6">{children}</div>
          {footer && <div className="mt-5 text-sm text-text-muted">{footer}</div>}
        </div>
      </div>
    </div>
  );
}
