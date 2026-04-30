import React, { useEffect, useMemo, useState, useCallback } from "react";
import { Link } from "react-router-dom";
import { useSelector } from "react-redux";
import { selectAuth } from "../../store/slices/authSlice";
import { PolicyService, KycService, ClaimService } from "../../lib/services";
import { extractErrorMessage } from "../../lib/api";
import ErrorAlert from "../../components/ui/ErrorAlert";
import StatusBadge from "../../components/ui/StatusBadge";
import { Skeleton } from "../../components/ui/Skeleton";
import { formatCurrency, formatStatusLabel } from "../../lib/constants";
import Icon from "../../components/ui/Icon";

export default function CustomerDashboard() {
  const { user } = useSelector(selectAuth);
  const [policies, setPolicies] = useState(null);
  const [kyc, setKyc] = useState(undefined);
  const [claims, setClaims] = useState(null);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    setError("");
    try {
      const [policyRes, kycRes, claimRes] = await Promise.all([
        PolicyService.myPurchases({ page: 0, size: 5 }).catch(() => ({ data: { content: [] } })),
        KycService.myStatus().catch((err) =>
          err?.response?.status === 404 ? { data: null } : Promise.reject(err)
        ),
        ClaimService.myClaims({ page: 0, size: 5 }).catch(() => ({ data: { content: [] } })),
      ]);
      setPolicies(policyRes.data?.content || []);
      setKyc(kycRes.data || null);
      setClaims(claimRes.data?.content || []);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load your dashboard."));
      setPolicies([]);
      setClaims([]);
      setKyc(null);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const totals = useMemo(() => {
    const activePolicies = (policies || []).filter(
      (policy) => String(policy.status).toUpperCase() === "ACTIVE"
    );
    const premium = activePolicies.reduce(
      (sum, policy) => sum + Number(policy.premiumAmount || 0),
      0
    );
    const openClaims = (claims || []).filter((claim) =>
      ["DRAFT", "SUBMITTED", "UNDER_REVIEW"].includes(String(claim.status).toUpperCase())
    );
    return {
      activePolicyCount: activePolicies.length,
      premium,
      openClaims: openClaims.length,
    };
  }, [claims, policies]);

  const loading = policies === null || claims === null || kyc === undefined;

  return (
    <div className="space-y-8">
      <div>
        <p className="text-xs uppercase tracking-widest text-text-subtle">Dashboard</p>
        <h1 className="font-heading text-4xl font-semibold mt-1">Welcome back, {getDisplayName(user)}</h1>
        <p className="mt-2 text-text-muted">Live snapshot of policies, KYC, and claim activity.</p>
      </div>

      {error && <ErrorAlert message={error} onRetry={load} />}

      <div className="grid sm:grid-cols-3 gap-4">
        <StatCard label="Active policies" value={loading ? null : totals.activePolicyCount} />
        <StatCard label="Open claims" value={loading ? null : totals.openClaims} />
        <StatCard label="Active premium" value={loading ? null : formatCurrency(totals.premium)} />
      </div>

      <section className="grid lg:grid-cols-3 gap-4">
        <QuickLink
          to="/app/my-policies"
          icon="file"
          title="My policies"
          body="View your purchased coverage and policy details."
        />
        <QuickLink
          to="/app/claims"
          icon="clipboard"
          title="My claims"
          body="Edit drafts, submit, and review current statuses."
        />
        <QuickLink
          to="/app/kyc"
          icon="id-card"
          title="KYC status"
          body={kyc?.status ? `Status: ${formatStatusLabel(kyc.status)}.` : "Complete verification to keep purchases and claims moving."}
          badge={kyc?.status ? <StatusBadge status={kyc.status} /> : null}
        />
      </section>

      <section className="grid lg:grid-cols-2 gap-4">
        <div className="rounded-2xl border border-border bg-surface p-5">
          <div className="flex items-center justify-between gap-3">
            <div>
              <p className="text-xs uppercase tracking-widest text-text-subtle">Recent policies</p>
              <h2 className="font-heading text-2xl font-semibold mt-1">Coverage on account</h2>
            </div>
            <Link to="/app/my-policies" className="text-sm font-semibold text-brand hover:underline">
              View all
            </Link>
          </div>
          <div className="mt-4 space-y-3">
            {loading ? (
              <Skeleton className="h-28 w-full" />
            ) : policies.length === 0 ? (
              <EmptyBox text="No purchased policies yet." />
            ) : (
              policies.slice(0, 3).map((policy) => (
                <div key={policy.id} className="rounded-xl bg-surface-2 p-4 flex items-center gap-3">
                  <div className="grid place-items-center w-10 h-10 rounded-md bg-brand-soft text-brand">
                    <Icon name={String(policy.policyType).toUpperCase() === "VEHICLE" ? "vehicle" : "home"} />
                  </div>
                  <div className="flex-1">
                    <p className="font-semibold">{policy.policyName}</p>
                    <p className="text-sm text-text-muted">
                      {formatCurrency(policy.premiumAmount)} · {formatStatusLabel(policy.status)}
                    </p>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="rounded-2xl border border-border bg-surface p-5">
          <div className="flex items-center justify-between gap-3">
            <div>
              <p className="text-xs uppercase tracking-widest text-text-subtle">Recent claims</p>
              <h2 className="font-heading text-2xl font-semibold mt-1">Claims pipeline</h2>
            </div>
            <Link to="/app/claims" className="text-sm font-semibold text-brand hover:underline">
              Open claims
            </Link>
          </div>
          <div className="mt-4 space-y-3">
            {loading ? (
              <Skeleton className="h-28 w-full" />
            ) : claims.length === 0 ? (
              <EmptyBox text="No claims created yet." />
            ) : (
              claims.slice(0, 3).map((claim) => (
                <div key={claim.id} className="rounded-xl bg-surface-2 p-4 flex items-center gap-3">
                  <div className="grid place-items-center w-10 h-10 rounded-md bg-brand-soft text-brand">
                    <Icon name="clipboard" />
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center gap-2 flex-wrap">
                      <p className="font-semibold">Claim #{claim.id}</p>
                      <StatusBadge status={claim.status} />
                    </div>
                    <p className="text-sm text-text-muted">{formatCurrency(claim.claimAmount)}</p>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </section>
    </div>
  );
}

function QuickLink({ to, icon, title, body, badge }) {
  return (
    <Link
      to={to}
      className="group rounded-2xl border border-border bg-surface p-6 hover:border-brand transition-colors"
    >
      <div className="flex items-center justify-between">
        <span className="text-brand">
          <Icon name={icon} />
        </span>
        {badge || <Icon name="arrow-right" className="text-text-subtle group-hover:text-brand" />}
      </div>
      <h3 className="font-heading text-xl font-semibold mt-4">{title}</h3>
      <p className="text-sm text-text-muted mt-1">{body}</p>
    </Link>
  );
}

function StatCard({ label, value }) {
  return (
    <div className="rounded-2xl border border-border bg-surface p-5">
      <p className="text-xs uppercase tracking-widest text-text-subtle">{label}</p>
      {value === null ? (
        <Skeleton className="h-8 w-24 mt-2" />
      ) : (
        <p className="font-heading text-3xl font-semibold mt-1">{value}</p>
      )}
    </div>
  );
}

function EmptyBox({ text }) {
  return <div className="rounded-xl bg-surface-2 p-4 text-sm text-text-muted">{text}</div>;
}

function getDisplayName(user) {
  if (user?.name && String(user.name).trim()) return String(user.name).trim();
  const email = String(user?.email || "").trim();
  if (!email) return "Customer";
  const localPart = email.split("@")[0] || "Customer";
  return localPart
    .replace(/[._-]+/g, " ")
    .replace(/\b\w/g, (char) => char.toUpperCase());
}
