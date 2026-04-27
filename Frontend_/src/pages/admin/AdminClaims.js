import React, { useEffect, useState, useCallback, memo } from "react";
import { useDispatch } from "react-redux";
import { ClipboardList } from "lucide-react";
import { AdminClaimService } from "../../lib/services";
import { extractErrorMessage } from "../../lib/api";
import ErrorAlert from "../../components/ui/ErrorAlert";
import StatusBadge from "../../components/ui/StatusBadge";
import { ListCardSkeleton } from "../../components/ui/Skeleton";
import EmptyState from "../../components/ui/EmptyState";
import Pagination from "../../components/ui/Pagination";
import Field from "../../components/ui/Field";
import Button from "../../components/ui/Button";
import { pushToast } from "../../store/slices/toastSlice";
import { CLAIM_STATUSES, formatCurrency } from "../../lib/constants";

const PAGE_SIZE = 10;

export default function AdminClaims() {
  const dispatch = useDispatch();
  const [page, setPage] = useState(0);
  const [filter, setFilter] = useState("");
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    setData(null);
    setError("");
    try {
      const params = { page, size: PAGE_SIZE };
      if (filter) params.status = filter;
      const { data: res } = await AdminClaimService.list(params);
      setData(res);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load claims."));
      setData({ content: [], totalPages: 1, number: 0 });
    }
  }, [page, filter]);

  useEffect(() => {
    load();
  }, [load]);

  const override = useCallback(
    async (claim, status) => {
      try {
        await AdminClaimService.override(claim.id, status);
        dispatch(pushToast({ message: `Claim ${status.toLowerCase()}.`, variant: "success" }));
        load();
      } catch (err) {
        dispatch(pushToast({ message: extractErrorMessage(err), variant: "danger" }));
      }
    },
    [dispatch, load]
  );

  const list = data?.content || [];

  return (
    <div className="space-y-6">
      <header>
        <p className="text-xs uppercase tracking-widest text-text-subtle">Operations</p>
        <h1 className="font-heading text-4xl font-semibold mt-1">Claims</h1>
      </header>

      <div className="flex items-center gap-3 flex-wrap">
        <Field
          as="select"
          id="claim-filter"
          value={filter}
          onChange={(e) => { setFilter(e.target.value); setPage(0); }}
          className="max-w-xs"
        >
          <option value="">All statuses</option>
          {CLAIM_STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
        </Field>
      </div>

      {error && <ErrorAlert message={error} onRetry={load} />}

      {data === null ? (
        <ListCardSkeleton />
      ) : list.length === 0 ? (
        <EmptyState icon={ClipboardList} title="No claims" />
      ) : (
        <ul className="space-y-3">
          {list.map((c) => (
            <ClaimRow key={c.id} claim={c} onOverride={override} />
          ))}
        </ul>
      )}

      {data && (
        <Pagination
          page={data.number ?? page}
          totalPages={data.totalPages ?? 1}
          onChange={setPage}
        />
      )}
    </div>
  );
}

const ClaimRow = memo(function ClaimRow({ claim, onOverride }) {
  return (
    <li className="rounded-xl border border-border bg-surface p-4 flex flex-wrap items-center gap-4">
      <div className="grid place-items-center w-10 h-10 rounded-md bg-brand-soft text-brand">
        <ClipboardList size={18} />
      </div>
      <div className="flex-1 min-w-[200px]">
        <div className="flex items-center gap-2 flex-wrap">
          <h3 className="font-semibold">Claim #{claim.id}</h3>
          <StatusBadge status={claim.status || "SUBMITTED"} />
        </div>
        <p className="text-sm text-text-muted mt-0.5">
          {claim.userEmail || claim.email || "—"} · {formatCurrency(claim.claimAmount || 0)}
        </p>
      </div>
      <div className="flex items-center gap-2">
        <Field
          as="select"
          id={`override-${claim.id}`}
          defaultValue=""
          onChange={(e) => {
            const v = e.target.value;
            if (v) onOverride(claim, v);
            e.target.value = "";
          }}
        >
          <option value="">Override status…</option>
          {CLAIM_STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
        </Field>
        <Button variant="outline" onClick={() => onOverride(claim, "APPROVED")}>Approve</Button>
        <Button variant="ghost" className="text-danger" onClick={() => onOverride(claim, "REJECTED")}>Reject</Button>
      </div>
    </li>
  );
});
