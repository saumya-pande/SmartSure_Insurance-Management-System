import React, { useEffect, useState, useCallback, memo } from "react";
import { Link } from "react-router-dom";
import { PolicyService } from "../../lib/services";
import { extractErrorMessage } from "../../lib/api";
import ErrorAlert from "../../components/ui/ErrorAlert";
import StatusBadge from "../../components/ui/StatusBadge";
import { ListCardSkeleton } from "../../components/ui/Skeleton";
import EmptyState from "../../components/ui/EmptyState";
import Pagination from "../../components/ui/Pagination";
import Icon from "../../components/ui/Icon";
import { formatCurrency, POLICY_TYPES } from "../../lib/constants";

const PAGE_SIZE = 8;

export default function BrowsePolicies({ publicMode = false }) {
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [typeFilter, setTypeFilter] = useState("");
  const [search, setSearch] = useState("");

  const load = useCallback(async () => {
    setData(null);
    setError("");
    try {
      const { data: response } = await PolicyService.listActive({ page, size: PAGE_SIZE });
      setData(response);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load policies."));
      setData({ content: [], totalElements: 0, totalPages: 1, number: 0 });
    }
  }, [page]);

  useEffect(() => {
    load();
  }, [load]);

  const list = data?.content || [];
  const filtered = list.filter((policy) => {
    const policyName = String(policy.policyName || policy.name || "").toLowerCase();
    const policyType = String(policy.type || policy.policyType || "").toUpperCase();
    const matchType = !typeFilter || policyType === typeFilter;
    const matchSearch = !search || policyName.includes(search.toLowerCase());
    return matchType && matchSearch;
  });

  return (
    <div className={`space-y-6 ${publicMode ? "mx-auto max-w-[1760px] px-5 sm:px-10 lg:px-20 py-12" : ""}`}>
      <header className="flex items-end justify-between flex-wrap gap-3">
        <div>
          <p className="text-xs uppercase tracking-widest text-text-subtle">Marketplace</p>
          <h1 className="font-heading text-4xl font-semibold mt-1">Browse policies</h1>
          <p className="mt-2 text-text-muted">
            {publicMode
              ? "View active home and vehicle policies. Sign in when you are ready to buy."
              : "Active home and vehicle policies available now."}
          </p>
        </div>
        {publicMode && (
          <Link
            to="/login"
            className="inline-flex items-center gap-2 rounded-md bg-brand px-4 py-2 text-sm font-semibold text-white hover:bg-brand-hover"
          >
            Sign in to buy
            <Icon name="arrow-right" />
          </Link>
        )}
      </header>

      <div className="flex flex-wrap items-center gap-2">
        <div className="relative flex-1 min-w-[220px]">
          <span className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted">
            <Icon name="search" />
          </span>
          <input
            value={search}
            onChange={(event) => setSearch(event.target.value)}
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
        {POLICY_TYPES.map((type) => (
          <button
            key={type}
            type="button"
            onClick={() => setTypeFilter(type)}
            className={`inline-flex items-center gap-1.5 px-3 py-2 text-sm rounded-md border ${typeFilter === type ? "bg-brand text-white border-brand" : "border-border bg-surface hover:bg-surface-2"}`}
          >
            <Icon name={type === "HOME" ? "home" : "vehicle"} />
            {type}
          </button>
        ))}
      </div>

      {error && <ErrorAlert message={error} onRetry={load} />}

      {data === null ? (
        <ListCardSkeleton count={5} />
      ) : filtered.length === 0 ? (
        <EmptyState
          icon="search"
          title="No policies match"
          description="Try different filters or another search term."
        />
      ) : (
        <ul className="space-y-3">
          {filtered.map((policy) => (
            <PolicyRow key={policy.id} policy={policy} publicMode={publicMode} />
          ))}
        </ul>
      )}

      {data && (
        <Pagination
          page={data.number ?? page}
          totalPages={data.totalPages ?? 1}
          onChange={(nextPage) => setPage(Math.max(0, nextPage))}
        />
      )}
    </div>
  );
}

const PolicyRow = memo(function PolicyRow({ policy, publicMode = false }) {
  const type = String(policy.type || policy.policyType || "").toUpperCase();
  const content = (
    <>
      <div className="grid place-items-center w-11 h-11 rounded-md bg-brand-soft text-brand">
        <Icon name={type === "VEHICLE" ? "vehicle" : "home"} />
      </div>
      <div className="flex-1 min-w-[200px]">
        <div className="flex items-center gap-2 flex-wrap">
          <h3 className="font-heading text-lg font-semibold">
            {policy.policyName || `Policy #${policy.id}`}
          </h3>
          <StatusBadge status={type || "POLICY"} />
          <StatusBadge status={policy.status || "ACTIVE"} />
        </div>
        <p className="text-sm text-text-muted mt-0.5">
          Base premium {formatCurrency(policy.basePremium || 0)} to max premium{" "}
          {formatCurrency(policy.maxPremium || 0)} · Max {policy.maxMonthCoverage} months
        </p>
        <p className="text-xs text-brand font-medium mt-1">
          Coverage: {formatCurrency(policy.minCoverageAmount)} - {formatCurrency(policy.maxCoverageAmount)}
        </p>
        {policy.description && (
          <p className="text-sm text-text-subtle mt-1.5 line-clamp-1 italic">
            "{policy.description}"
          </p>
        )}
      </div>
      <div className="text-right">
        <p className="text-xs text-text-subtle uppercase tracking-wider">Starting from</p>
        <p className="font-heading text-xl font-semibold">
          {formatCurrency(policy.basePremium || 0)}
          <span className="text-xs text-text-muted font-normal ml-1">
            / {policy.billingCycle === "MONTHLY" ? "mo" : "yr"}
          </span>
        </p>
      </div>
    </>
  );

  if (publicMode) {
    return (
      <li>
        <div className="flex flex-wrap items-center gap-4 rounded-xl border border-border bg-surface p-4">
          {content}
        </div>
      </li>
    );
  }

  return (
    <li>
      <Link
        to={`/app/policies/${policy.id}`}
        className="flex flex-wrap items-center gap-4 rounded-xl border border-border bg-surface p-4 hover:border-brand transition-colors"
      >
        {content}
      </Link>
    </li>
  );
});
