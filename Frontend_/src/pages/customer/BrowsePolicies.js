import React, { useEffect, useState, useCallback, memo } from "react";
import { Link } from "react-router-dom";
import { Home, Car, Search } from "lucide-react";
import { PolicyService } from "../../lib/services";
import { extractErrorMessage } from "../../lib/api";
import ErrorAlert from "../../components/ui/ErrorAlert";
import StatusBadge from "../../components/ui/StatusBadge";
import { ListCardSkeleton } from "../../components/ui/Skeleton";
import EmptyState from "../../components/ui/EmptyState";
import Pagination from "../../components/ui/Pagination";
import { formatCurrency, POLICY_TYPES } from "../../lib/constants";

const PAGE_SIZE = 8;

export default function BrowsePolicies() {
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [typeFilter, setTypeFilter] = useState("");
  const [search, setSearch] = useState("");

  const load = useCallback(async () => {
    setData(null);
    setError("");
    try {
      const { data: res } = await PolicyService.listActive({ page, size: PAGE_SIZE });
      setData(res);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load policies."));
      setData({ content: [], totalElements: 0, totalPages: 1, number: 0 });
    }
  }, [page]);

  useEffect(() => {
    load();
  }, [load]);

  const list = data?.content || [];
  const filtered = list.filter((p) => {
    const matchType = !typeFilter || String(p.policyType || p.type).toUpperCase() === typeFilter;
    const matchSearch =
      !search ||
      String(p.name || p.title || "").toLowerCase().includes(search.toLowerCase());
    return matchType && matchSearch;
  });

  return (
    <div className="space-y-6">
      <header className="flex items-end justify-between flex-wrap gap-3">
        <div>
          <p className="text-xs uppercase tracking-widest text-text-subtle">Marketplace</p>
          <h1 className="font-heading text-4xl font-semibold mt-1">Browse policies</h1>
          <p className="mt-2 text-text-muted">Active home and vehicle policies available now.</p>
        </div>
      </header>

      <div className="flex flex-wrap items-center gap-2">
        <div className="relative flex-1 min-w-[220px]">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search policies"
            aria-label="Search policies"
            className="w-full pl-9 pr-3 py-2 rounded-md border border-border bg-surface text-text focus:border-brand focus:outline-none"
          />
        </div>
        <button
          type="button"
          onClick={() => setTypeFilter("")}
          className={`px-3 py-2 text-sm rounded-md border ${typeFilter === "" ? "bg-brand text-white border-brand" : "border-border bg-surface hover:bg-surface-2"}`}
        >
          All
        </button>
        {POLICY_TYPES.map((t) => (
          <button
            key={t}
            type="button"
            onClick={() => setTypeFilter(t)}
            className={`inline-flex items-center gap-1.5 px-3 py-2 text-sm rounded-md border ${typeFilter === t ? "bg-brand text-white border-brand" : "border-border bg-surface hover:bg-surface-2"}`}
          >
            {t === "HOME" ? <Home size={14} /> : <Car size={14} />}
            {t}
          </button>
        ))}
      </div>

      {error && <ErrorAlert message={error} onRetry={load} />}

      {data === null ? (
        <ListCardSkeleton count={5} />
      ) : filtered.length === 0 ? (
        <EmptyState
          icon={Search}
          title="No policies match"
          description="Try adjusting filters or search to see more results."
        />
      ) : (
        <ul className="space-y-3">
          {filtered.map((p) => (
            <PolicyRow key={p.id} policy={p} />
          ))}
        </ul>
      )}

      {data && (
        <Pagination
          page={data.number ?? page}
          totalPages={data.totalPages ?? 1}
          onChange={(n) => setPage(Math.max(0, n))}
        />
      )}
    </div>
  );
}

const PolicyRow = memo(function PolicyRow({ policy }) {
  const type = String(policy.policyType || policy.type || "").toUpperCase();
  const Icon = type === "VEHICLE" ? Car : Home;
  return (
    <li>
      <Link
        to={`/app/policies/${policy.id}`}
        className="flex flex-wrap items-center gap-4 rounded-xl border border-border bg-surface p-4 hover:border-brand transition-colors"
      >
        <div className="grid place-items-center w-11 h-11 rounded-md bg-brand-soft text-brand">
          <Icon size={18} />
        </div>
        <div className="flex-1 min-w-[200px]">
          <div className="flex items-center gap-2 flex-wrap">
            <h3 className="font-heading text-lg font-semibold">
              {policy.name || policy.title || `Policy #${policy.id}`}
            </h3>
            <StatusBadge status={type || "POLICY"} />
            <StatusBadge status={policy.status || "ACTIVE"} />
          </div>
          <p className="text-sm text-text-muted mt-0.5 line-clamp-1">
            {policy.description || "Comprehensive coverage with flexible terms."}
          </p>
        </div>
        <div className="text-right">
          <p className="text-xs text-text-subtle uppercase tracking-wider">Premium</p>
          <p className="font-heading text-xl font-semibold">
            {formatCurrency(policy.premium || policy.premiumAmount || 0)}
          </p>
        </div>
      </Link>
    </li>
  );
});
