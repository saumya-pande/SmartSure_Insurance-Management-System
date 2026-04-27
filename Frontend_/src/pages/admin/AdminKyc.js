import React, { useEffect, useState, useCallback, memo } from "react";
import { useDispatch } from "react-redux";
import { ShieldCheck } from "lucide-react";
import { AdminKycService } from "../../lib/services";
import { extractErrorMessage } from "../../lib/api";
import ErrorAlert from "../../components/ui/ErrorAlert";
import StatusBadge from "../../components/ui/StatusBadge";
import { ListCardSkeleton } from "../../components/ui/Skeleton";
import EmptyState from "../../components/ui/EmptyState";
import Pagination from "../../components/ui/Pagination";
import Button from "../../components/ui/Button";
import { pushToast } from "../../store/slices/toastSlice";
import { KYC_STATUSES } from "../../lib/constants";

const PAGE_SIZE = 10;

export default function AdminKyc() {
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
      const { data: res } = await AdminKycService.list(params);
      setData(res);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load KYC submissions."));
      setData({ content: [], totalPages: 1, number: 0 });
    }
  }, [page, filter]);

  useEffect(() => {
    load();
  }, [load]);

  const setStatus = useCallback(
    async (item, status) => {
      try {
        await AdminKycService.setStatus(item.id, status);
        dispatch(pushToast({ message: `KYC ${status.toLowerCase()}.`, variant: "success" }));
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
        <h1 className="font-heading text-4xl font-semibold mt-1">KYC Management</h1>
      </header>

      <div className="flex flex-wrap gap-2">
        <FilterChip active={filter === ""} onClick={() => { setFilter(""); setPage(0); }}>All</FilterChip>
        {KYC_STATUSES.map((s) => (
          <FilterChip key={s} active={filter === s} onClick={() => { setFilter(s); setPage(0); }}>{s}</FilterChip>
        ))}
      </div>

      {error && <ErrorAlert message={error} onRetry={load} />}

      {data === null ? (
        <ListCardSkeleton />
      ) : list.length === 0 ? (
        <EmptyState icon={ShieldCheck} title="No submissions" />
      ) : (
        <ul className="space-y-3">
          {list.map((it) => (
            <KycRow key={it.id} item={it} onSetStatus={setStatus} />
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

function FilterChip({ active, onClick, children }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`px-3 py-1.5 text-sm rounded-md border ${active ? "bg-brand text-white border-brand" : "border-border bg-surface hover:bg-surface-2"}`}
    >
      {children}
    </button>
  );
}

const KycRow = memo(function KycRow({ item, onSetStatus }) {
  const approve = useCallback(() => onSetStatus(item, "APPROVED"), [onSetStatus, item]);
  const reject = useCallback(() => onSetStatus(item, "REJECTED"), [onSetStatus, item]);
  return (
    <li className="rounded-xl border border-border bg-surface p-4 flex flex-wrap items-center gap-4">
      <div className="grid place-items-center w-10 h-10 rounded-md bg-brand-soft text-brand">
        <ShieldCheck size={18} />
      </div>
      <div className="flex-1 min-w-[200px]">
        <div className="flex items-center gap-2 flex-wrap">
          <h3 className="font-semibold">{item.userEmail || item.email || `Submission #${item.id}`}</h3>
          <StatusBadge status={item.status || "PENDING"} />
        </div>
        <p className="text-sm text-text-muted mt-0.5">
          {item.documentType ? `Type: ${item.documentType}` : ""} {item.address ? `· ${item.address}` : ""}
        </p>
      </div>
      <div className="flex gap-2">
        <Button variant="outline" onClick={reject}>Reject</Button>
        <Button onClick={approve}>Approve</Button>
      </div>
    </li>
  );
});
