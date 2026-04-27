import React, { useEffect, useState, useCallback } from "react";
import { Users, FileText, DollarSign, ClipboardList } from "lucide-react";
import { AdminUserService, AdminPolicyService, AdminKycService } from "../../lib/services";
import { extractErrorMessage } from "../../lib/api";
import ErrorAlert from "../../components/ui/ErrorAlert";
import { Skeleton } from "../../components/ui/Skeleton";
import { formatCurrency } from "../../lib/constants";

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    setStats(null);
    setError("");
    try {
      const [u, p, r, k] = await Promise.all([
        AdminUserService.count().catch(() => ({ data: 0 })),
        AdminPolicyService.count().catch(() => ({ data: 0 })),
        AdminPolicyService.revenue().catch(() => ({ data: 0 })),
        AdminKycService.count().catch(() => ({ data: 0 })),
      ]);
      setStats({
        users: extractNum(u.data),
        policies: extractNum(p.data),
        revenue: extractNum(r.data),
        kyc: extractNum(k.data),
      });
    } catch (err) {
      setError(extractErrorMessage(err));
      setStats({ users: 0, policies: 0, revenue: 0, kyc: 0 });
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <div className="space-y-8">
      <header>
        <p className="text-xs uppercase tracking-widest text-text-subtle">Administration</p>
        <h1 className="font-heading text-4xl font-semibold mt-1">Overview</h1>
        <p className="mt-2 text-text-muted">Operational health of SmartSure at a glance.</p>
      </header>

      {error && <ErrorAlert message={error} onRetry={load} />}

      <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <Stat icon={Users} label="Users" value={stats?.users} />
        <Stat icon={FileText} label="Policies" value={stats?.policies} />
        <Stat icon={ClipboardList} label="KYC submissions" value={stats?.kyc} />
        <Stat
          icon={DollarSign}
          label="Revenue"
          value={stats?.revenue !== undefined ? formatCurrency(stats.revenue) : undefined}
        />
      </div>
    </div>
  );
}

function extractNum(v) {
  if (typeof v === "number") return v;
  if (v && typeof v === "object") return v.count ?? v.total ?? v.value ?? 0;
  const n = Number(v);
  return Number.isFinite(n) ? n : 0;
}

function Stat({ icon: Icon, label, value }) {
  return (
    <div className="rounded-2xl border border-border bg-surface p-5">
      <div className="flex items-center justify-between">
        <p className="text-xs uppercase tracking-widest text-text-subtle">{label}</p>
        <Icon size={18} className="text-text-muted" />
      </div>
      {value === undefined ? (
        <Skeleton className="h-8 w-20 mt-2" />
      ) : (
        <p className="font-heading text-3xl font-semibold mt-1">{value}</p>
      )}
    </div>
  );
}
