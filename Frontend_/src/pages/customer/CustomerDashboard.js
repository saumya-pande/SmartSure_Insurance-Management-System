import React, { useEffect, useMemo, useState, useCallback } from "react";
import { Link } from "react-router-dom";
import { useSelector } from "react-redux";
import { ArrowRight, FileText, ShoppingBag, ShieldCheck } from "lucide-react";
import { selectAuth } from "../../store/slices/authSlice";
import { PolicyService, KycService } from "../../lib/services";
import { extractErrorMessage } from "../../lib/api";
import ErrorAlert from "../../components/ui/ErrorAlert";
import StatusBadge from "../../components/ui/StatusBadge";
import { Skeleton } from "../../components/ui/Skeleton";
import { formatCurrency } from "../../lib/constants";

export default function CustomerDashboard() {
  const { user } = useSelector(selectAuth);
  const [policies, setPolicies] = useState(null);
  const [kyc, setKyc] = useState(null);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    setError("");
    try {
      const [pRes, kRes] = await Promise.all([
        PolicyService.myPurchases().catch(() => ({ data: [] })),
        KycService.myStatus().catch(() => ({ data: null })),
      ]);
      setPolicies(Array.isArray(pRes.data) ? pRes.data : pRes.data?.content || []);
      setKyc(kRes.data || null);
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const totals = useMemo(() => {
    const list = policies || [];
    const active = list.filter((p) => String(p.status).toUpperCase() === "ACTIVE").length;
    const premium = list.reduce((sum, p) => sum + Number(p.premium || p.premiumAmount || 0), 0);
    return { active, total: list.length, premium };
  }, [policies]);

  const loading = policies === null || kyc === null;

  return (
    <div className="space-y-8">
      <div>
        <p className="text-xs uppercase tracking-widest text-text-subtle">Welcome back</p>
        <h1 className="font-heading text-4xl font-semibold mt-1">
          {user?.name || "There"}.
        </h1>
        <p className="mt-2 text-text-muted">Here's a snapshot of your coverage today.</p>
      </div>

      {error && <ErrorAlert message={error} onRetry={load} />}

      <div className="grid sm:grid-cols-3 gap-4">
        <StatCard label="Active policies" value={loading ? null : totals.active} />
        <StatCard label="Total policies" value={loading ? null : totals.total} />
        <StatCard
          label="Total premium"
          value={loading ? null : formatCurrency(totals.premium)}
        />
      </div>

      <section className="grid lg:grid-cols-3 gap-4">
        <Link
          to="/app/policies"
          className="group rounded-2xl border border-border bg-surface p-6 hover:border-brand transition-colors"
        >
          <div className="flex items-center justify-between">
            <ShoppingBag className="text-brand" size={20} />
            <ArrowRight size={16} className="text-text-subtle group-hover:text-brand transition-transform group-hover:translate-x-1" />
          </div>
          <h3 className="font-heading text-xl font-semibold mt-4">Browse policies</h3>
          <p className="text-sm text-text-muted mt-1">Explore home and vehicle plans.</p>
        </Link>
        <Link
          to="/app/my-policies"
          className="group rounded-2xl border border-border bg-surface p-6 hover:border-brand transition-colors"
        >
          <div className="flex items-center justify-between">
            <FileText className="text-brand" size={20} />
            <ArrowRight size={16} className="text-text-subtle group-hover:text-brand transition-transform group-hover:translate-x-1" />
          </div>
          <h3 className="font-heading text-xl font-semibold mt-4">My policies</h3>
          <p className="text-sm text-text-muted mt-1">Review and manage what you own.</p>
        </Link>
        <Link
          to="/app/kyc"
          className="group rounded-2xl border border-border bg-surface p-6 hover:border-brand transition-colors"
        >
          <div className="flex items-center justify-between">
            <ShieldCheck className="text-brand" size={20} />
            {kyc?.status && <StatusBadge status={kyc.status} />}
          </div>
          <h3 className="font-heading text-xl font-semibold mt-4">KYC status</h3>
          <p className="text-sm text-text-muted mt-1">
            {kyc?.status
              ? `Your verification is ${String(kyc.status).toLowerCase()}.`
              : "Submit your documents to unlock claims."}
          </p>
        </Link>
      </section>
    </div>
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
