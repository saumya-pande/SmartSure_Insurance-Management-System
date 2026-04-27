import React, { useEffect, useState, useCallback, memo } from "react";
import { Link } from "react-router-dom";
import { Car, FileText, Home } from "lucide-react";
import { PolicyService } from "../../lib/services";
import { extractErrorMessage } from "../../lib/api";
import ErrorAlert from "../../components/ui/ErrorAlert";
import StatusBadge from "../../components/ui/StatusBadge";
import { ListCardSkeleton } from "../../components/ui/Skeleton";
import EmptyState from "../../components/ui/EmptyState";
import { formatCurrency } from "../../lib/constants";

export default function MyPolicies() {
  const [items, setItems] = useState(null);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    setItems(null);
    setError("");
    try {
      const { data } = await PolicyService.myPurchases();
      setItems(Array.isArray(data) ? data : data?.content || []);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load your policies."));
      setItems([]);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <div className="space-y-6">
      <header>
        <p className="text-xs uppercase tracking-widest text-text-subtle">Your portfolio</p>
        <h1 className="font-heading text-4xl font-semibold mt-1">My policies</h1>
        <p className="mt-2 text-text-muted">Active and historical policies on your account.</p>
      </header>

      {error && <ErrorAlert message={error} onRetry={load} />}

      {items === null ? (
        <ListCardSkeleton />
      ) : items.length === 0 ? (
        <EmptyState
          icon={FileText}
          title="No policies yet"
          description="Browse our marketplace to find a policy that fits."
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
          {items.map((p) => (
            <PurchasedRow key={p.id} purchase={p} />
          ))}
        </ul>
      )}
    </div>
  );
}

const PurchasedRow = memo(function PurchasedRow({ purchase }) {
  const type = String(purchase.policyType || purchase.type || "").toUpperCase();
  const Icon = type === "VEHICLE" ? Car : Home;
  return (
    <li className="rounded-xl border border-border bg-surface p-4 flex flex-wrap items-center gap-4">
      <div className="grid place-items-center w-11 h-11 rounded-md bg-brand-soft text-brand">
        <Icon size={18} />
      </div>
      <div className="flex-1 min-w-[200px]">
        <div className="flex items-center gap-2 flex-wrap">
          <h3 className="font-heading text-lg font-semibold">
            {purchase.policyName || purchase.name || `Policy #${purchase.policyId || purchase.id}`}
          </h3>
          <StatusBadge status={type || "POLICY"} />
          <StatusBadge status={purchase.status || "ACTIVE"} />
        </div>
        <p className="text-sm text-text-muted mt-0.5">
          Purchased on{" "}
          {purchase.purchaseDate
            ? new Date(purchase.purchaseDate).toLocaleDateString()
            : "—"}
        </p>
      </div>
      <div className="text-right">
        <p className="text-xs text-text-subtle uppercase tracking-wider">Premium</p>
        <p className="font-heading text-xl font-semibold">
          {formatCurrency(purchase.premium || purchase.premiumAmount || 0)}
        </p>
      </div>
      <Link
        to={`/app/claims/new?policy=${purchase.id}`}
        className="px-3 py-2 text-sm font-semibold rounded-md border border-border hover:bg-surface-2"
      >
        File claim
      </Link>
    </li>
  );
});
