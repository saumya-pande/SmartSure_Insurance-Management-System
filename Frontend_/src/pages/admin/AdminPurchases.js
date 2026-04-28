import React, { useEffect, useState, useCallback } from "react";
import { AdminPolicyService } from "../../lib/services";
import { extractErrorMessage } from "../../lib/api";
import ErrorAlert from "../../components/ui/ErrorAlert";
import StatusBadge from "../../components/ui/StatusBadge";
import { ListCardSkeleton } from "../../components/ui/Skeleton";
import EmptyState from "../../components/ui/EmptyState";
import Pagination from "../../components/ui/Pagination";
import Field from "../../components/ui/Field";
import { POLICY_TYPES, PURCHASE_STATUSES, formatCurrency } from "../../lib/constants";

const PAGE_SIZE = 10;

export default function AdminPurchases() {
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [filters, setFilters] = useState({
    email: "",
    policyType: "",
    status: "",
    minPremium: "",
    maxPremium: "",
    startDate: "",
    endDate: "",
  });

  const load = useCallback(async () => {
    setData(null);
    setError("");
    try {
      const params = { page, size: PAGE_SIZE };
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== "") params[key] = value;
      });
      const { data: response } = await AdminPolicyService.listPurchased(params);
      setData(response);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load purchased policies."));
      setData({ content: [], totalPages: 1, number: 0 });
    }
  }, [filters, page]);

  useEffect(() => {
    load();
  }, [load]);

  const purchases = data?.content || [];

  return (
    <div className="space-y-6">
      <header>
        <p className="text-xs uppercase tracking-widest text-text-subtle">Portfolio</p>
        <h1 className="font-heading text-4xl font-semibold mt-1">Purchased policies</h1>
      </header>

      <section className="rounded-2xl border border-border bg-surface p-4 grid md:grid-cols-4 gap-3">
        <Field id="purchase-email" label="Customer email" value={filters.email} onChange={(event) => setFilters((current) => ({ ...current, email: event.target.value }))} />
        <Field as="select" id="purchase-type" label="Type" value={filters.policyType} onChange={(event) => setFilters((current) => ({ ...current, policyType: event.target.value }))}>
          <option value="">All types</option>
          {POLICY_TYPES.map((type) => (
            <option key={type} value={type}>{type}</option>
          ))}
        </Field>
        <Field as="select" id="purchase-status" label="Status" value={filters.status} onChange={(event) => setFilters((current) => ({ ...current, status: event.target.value }))}>
          <option value="">All statuses</option>
          {PURCHASE_STATUSES.map((status) => (
            <option key={status} value={status}>{status}</option>
          ))}
        </Field>
        <Field id="purchase-min" label="Min premium" type="number" value={filters.minPremium} onChange={(event) => setFilters((current) => ({ ...current, minPremium: event.target.value }))} />
        <Field id="purchase-max" label="Max premium" type="number" value={filters.maxPremium} onChange={(event) => setFilters((current) => ({ ...current, maxPremium: event.target.value }))} />
        <Field id="purchase-start" label="Start date" type="date" value={filters.startDate} onChange={(event) => setFilters((current) => ({ ...current, startDate: event.target.value }))} />
        <Field id="purchase-end" label="End date" type="date" value={filters.endDate} onChange={(event) => setFilters((current) => ({ ...current, endDate: event.target.value }))} />
      </section>

      {error && <ErrorAlert message={error} onRetry={load} />}

      {data === null ? (
        <ListCardSkeleton />
      ) : purchases.length === 0 ? (
        <EmptyState icon="wallet" title="No purchased policies found" />
      ) : (
        <ul className="space-y-3">
          {purchases.map((purchase) => (
            <li key={purchase.id} className="rounded-xl border border-border bg-surface p-4 flex flex-wrap items-center gap-4">
              <div className="flex-1 min-w-[240px]">
                <div className="flex items-center gap-2 flex-wrap">
                  <h3 className="font-heading text-lg font-semibold">{purchase.policyName}</h3>
                  <StatusBadge status={purchase.policyType} />
                  <StatusBadge status={purchase.status} />
                </div>
                <p className="text-sm text-text-muted mt-1">
                  {purchase.customerEmail} · {purchase.holderName}
                </p>
                <p className="text-sm text-text-muted">
                  {purchase.startDate} to {purchase.endDate} · Property ID: {purchase.propertyIdentifier}
                </p>
              </div>
              <div className="text-right">
                <p className="text-xs text-text-subtle uppercase tracking-wider">Premium</p>
                <p className="font-heading text-xl font-semibold">{formatCurrency(purchase.premiumAmount)}</p>
              </div>
            </li>
          ))}
        </ul>
      )}

      {data && <Pagination page={data.number ?? page} totalPages={data.totalPages ?? 1} onChange={setPage} />}
    </div>
  );
}
