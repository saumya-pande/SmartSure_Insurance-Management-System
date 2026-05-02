import React, { useEffect, useState, useCallback, memo } from "react";
import { Link } from "react-router-dom";
import { PolicyService } from "../../lib/services";
import { extractErrorMessage } from "../../lib/api";
import ErrorAlert from "../../components/ui/ErrorAlert";
import StatusBadge from "../../components/ui/StatusBadge";
import { ListCardSkeleton } from "../../components/ui/Skeleton";
import EmptyState from "../../components/ui/EmptyState";
import Pagination from "../../components/ui/Pagination";
import { formatCurrency, formatStatusLabel } from "../../lib/constants";
import Icon from "../../components/ui/Icon";

const PAGE_SIZE = 8;

export default function MyPolicies() {
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    setData(null);
    setError("");
    try {
      const { data: response } = await PolicyService.myPurchases({ page, size: PAGE_SIZE });
      setData(response);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load your policies."));
      setData({ content: [], totalPages: 1, number: 0 });
    }
  }, [page]);

  useEffect(() => {
    load();
  }, [load]);

  const policies = data?.content || [];

  return (
    <div className="space-y-6">
      <header>
        <p className="text-xs uppercase tracking-widest text-text-subtle">Your portfolio</p>
        <h1 className="font-heading text-4xl font-semibold mt-1">My policies</h1>
        <p className="mt-2 text-text-muted">Purchased coverage, active dates, and next action.</p>
      </header>

      {error && <ErrorAlert message={error} onRetry={load} />}

      {data === null ? (
        <ListCardSkeleton />
      ) : policies.length === 0 ? (
        <EmptyState
          icon="file"
          title="No policies yet"
          description="Browse active policies and purchase coverage first."
          action={
            <Link
              to="/app/policies"
              className="inline-flex px-4 py-2 rounded-md bg-brand text-white font-semibold hover:bg-brand-hover"
            >
              Browse policies
            </Link>
          }
        />
      ) : (
        <ul className="space-y-3">
          {policies.map((policy) => (
            <PolicyRow key={policy.id} policy={policy} />
          ))}
        </ul>
      )}

      {data && (
        <Pagination page={data.number ?? page} totalPages={data.totalPages ?? 1} onChange={setPage} />
      )}
    </div>
  );
}

const PolicyRow = memo(function PolicyRow({ policy }) {
  const isVehicle = String(policy.policyType).toUpperCase() === "VEHICLE";

  return (
    <li className="rounded-xl border border-border bg-surface p-4 flex flex-wrap items-center gap-4">
      <div className="grid place-items-center w-11 h-11 rounded-md bg-brand-soft text-brand">
        <Icon name={isVehicle ? "vehicle" : "home"} />
      </div>
      <div className="flex-1 min-w-[220px]">
        <div className="flex items-center gap-2 flex-wrap">
          <h3 className="font-heading text-lg font-semibold">{policy.policyName}</h3>
          <StatusBadge status={policy.policyType} />
          <StatusBadge status={policy.status} />
        </div>
        <p className="text-sm text-text-muted mt-1">
          Holder: {policy.holderName} · Property ID: {policy.propertyIdentifier}
        </p>
        <p className="text-sm font-semibold text-brand">
          Coverage: {formatCurrency(policy.coverageAmount)}
        </p>
        <p className="text-sm text-text-muted">
          {new Date(policy.startDate).toLocaleDateString()} to{" "}
          {new Date(policy.endDate).toLocaleDateString()} · {formatStatusLabel(policy.status)}
        </p>
      </div>
      <div className="text-right">
        <p className="text-xs text-text-subtle uppercase tracking-wider">Premium</p>
        <p className="font-heading text-xl font-semibold">{formatCurrency(policy.premiumAmount)}</p>
      </div>
      <Link
        to={`/app/claims/new?policy=${policy.id}`}
        className="px-3 py-2 text-sm font-semibold rounded-md border border-border hover:bg-surface-2"
      >
        File claim
      </Link>
    </li>
  );
});
