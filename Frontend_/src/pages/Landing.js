import React from "react";
import { Link } from "react-router-dom";
import { ShieldCheck, Zap, Lock, ArrowRight } from "lucide-react";

export default function Landing() {
  return (
    <div>
      {/* Hero */}
      <section className="relative overflow-hidden">
        <div className="absolute inset-0 -z-10 bg-gradient-to-br from-bg via-bg to-brand-soft/30" />
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-20 sm:py-28 grid lg:grid-cols-2 gap-12 items-center">
          <div>
            <span className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-surface border border-border text-xs font-semibold text-text-muted">
              <span className="w-1.5 h-1.5 rounded-full bg-success" />
              Insurance, simplified.
            </span>
            <h1 className="mt-5 font-heading text-5xl sm:text-6xl font-semibold leading-[1.05] tracking-tight">
              Coverage you understand.
              <br />
              <span className="text-brand">Confidence you can feel.</span>
            </h1>
            <p className="mt-5 text-lg text-text-muted max-w-xl">
              SmartSure brings home and vehicle insurance into one calm,
              transparent dashboard — buy policies, file claims, and track
              everything in real time.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Link
                to="/register"
                className="inline-flex items-center gap-2 px-5 py-3 rounded-md bg-brand text-white font-semibold hover:bg-brand-hover transition-colors"
              >
                Create an account
                <ArrowRight size={16} />
              </Link>
              <Link
                to="/login"
                className="inline-flex items-center gap-2 px-5 py-3 rounded-md border border-border bg-surface font-semibold hover:bg-surface-2 transition-colors"
              >
                Sign in
              </Link>
            </div>
          </div>
          <div className="relative">
            <div className="rounded-3xl border border-border bg-surface shadow-elevated p-6">
              <div className="flex items-center justify-between">
                <p className="text-xs uppercase tracking-widest text-text-subtle">Active policy</p>
                <span className="px-2 py-0.5 rounded-md bg-success-soft text-success text-xs font-semibold">
                  ACTIVE
                </span>
              </div>
              <h3 className="font-heading text-2xl font-semibold mt-2">
                SmartSure Vehicle • Premium
              </h3>
              <p className="text-sm text-text-muted mt-1">Renews 03 Jul 2026</p>
              <div className="mt-6 grid grid-cols-3 gap-3">
                {[
                  { k: "Coverage", v: "$50,000" },
                  { k: "Premium", v: "$84/mo" },
                  { k: "Deductible", v: "$500" },
                ].map((s) => (
                  <div key={s.k} className="rounded-lg bg-surface-2 p-3">
                    <p className="text-[11px] uppercase text-text-subtle tracking-wider">
                      {s.k}
                    </p>
                    <p className="font-heading text-xl font-semibold mt-1">{s.v}</p>
                  </div>
                ))}
              </div>
              <div className="mt-6 h-2 rounded-full bg-surface-2 overflow-hidden">
                <div className="h-full w-3/4 bg-brand" />
              </div>
              <p className="mt-2 text-xs text-text-muted">75% of policy term complete</p>
            </div>
            <div className="absolute -top-6 -right-6 hidden sm:block w-32 h-32 rounded-full bg-brand/20 blur-2xl" />
            <div className="absolute -bottom-6 -left-6 hidden sm:block w-40 h-40 rounded-full bg-accent/20 blur-3xl" />
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="border-t border-border bg-surface">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-20">
          <div className="max-w-2xl">
            <p className="text-xs font-semibold tracking-widest uppercase text-brand">
              Built for clarity
            </p>
            <h2 className="font-heading text-3xl sm:text-4xl font-semibold mt-2">
              Everything you need. Nothing you don't.
            </h2>
          </div>
          <div className="mt-10 grid md:grid-cols-3 gap-4">
            {FEATURES.map((f) => {
              const Icon = f.icon;
              return (
                <div
                  key={f.title}
                  className="rounded-2xl border border-border bg-bg p-6 hover:border-border-strong transition-colors"
                >
                  <div className="grid place-items-center w-10 h-10 rounded-md bg-brand-soft text-brand">
                    <Icon size={18} />
                  </div>
                  <h3 className="font-heading text-xl font-semibold mt-4">{f.title}</h3>
                  <p className="text-sm text-text-muted mt-1">{f.body}</p>
                </div>
              );
            })}
          </div>
        </div>
      </section>
    </div>
  );
}

const FEATURES = [
  {
    icon: ShieldCheck,
    title: "Real coverage",
    body: "Home and vehicle policies underwritten by trusted partners, with terms you can actually read.",
  },
  {
    icon: Zap,
    title: "Fast claims",
    body: "File a claim in minutes, upload evidence, and track every status change in real time.",
  },
  {
    icon: Lock,
    title: "Verified identity",
    body: "Built-in KYC keeps your account secure without slowing you down.",
  },
];
