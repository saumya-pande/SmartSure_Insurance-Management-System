import React, { useEffect, useState, useCallback } from "react";
import { useDispatch } from "react-redux";
import { AdminKycService, KycService } from "../../lib/services";
import { extractErrorMessage } from "../../lib/api";
import ErrorAlert from "../../components/ui/ErrorAlert";
import StatusBadge from "../../components/ui/StatusBadge";
import { ListCardSkeleton } from "../../components/ui/Skeleton";
import EmptyState from "../../components/ui/EmptyState";
import Pagination from "../../components/ui/Pagination";
import Button from "../../components/ui/Button";
import Field from "../../components/ui/Field";
import { pushToast } from "../../store/slices/toastSlice";
import { KYC_STATUSES } from "../../lib/constants";
import { openBlobResponse } from "../../lib/file";

const PAGE_SIZE = 10;

export default function AdminKyc() {
  const dispatch = useDispatch();
  const [page, setPage] = useState(0);
  const [filters, setFilters] = useState({ status: "", email: "" });
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    setData(null);
    setError("");
    try {
      const params = { page, size: PAGE_SIZE };
      if (filters.status) params.status = filters.status;
      if (filters.email) params.email = filters.email;
      const { data: response } = await AdminKycService.list(params);
      setData(response);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load KYC submissions."));
      setData({ content: [], totalPages: 1, number: 0 });
    }
  }, [filters, page]);

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

  const openFile = useCallback(
    async (id) => {
      try {
        const response = await KycService.adminFile(id);
        openBlobResponse(response);
      } catch (err) {
        dispatch(pushToast({ message: extractErrorMessage(err, "Could not open file."), variant: "danger" }));
      }
    },
    [dispatch]
  );

  const items = data?.content || [];

  return (
    <div className="space-y-6">
      <header>
        <p className="text-xs uppercase tracking-widest text-text-subtle">Operations</p>
        <h1 className="font-heading text-4xl font-semibold mt-1">KYC Management</h1>
      </header>

      <section className="rounded-2xl border border-border bg-surface p-4 grid md:grid-cols-2 gap-3">
        <Field id="kyc-email" label="Customer email" value={filters.email} onChange={(event) => setFilters((current) => ({ ...current, email: event.target.value }))} />
        <Field as="select" id="kyc-status" label="Status" value={filters.status} onChange={(event) => setFilters((current) => ({ ...current, status: event.target.value }))}>
          <option value="">All statuses</option>
          {KYC_STATUSES.map((status) => (
            <option key={status} value={status}>{status}</option>
          ))}
        </Field>
      </section>

      {error && <ErrorAlert message={error} onRetry={load} />}

      {data === null ? (
        <ListCardSkeleton />
      ) : items.length === 0 ? (
        <EmptyState icon="shield" title="No submissions" />
      ) : (
        <ul className="space-y-3">
          {items.map((item) => (
            <li key={item.id} className="rounded-xl border border-border bg-surface p-4 flex flex-wrap items-center gap-4">
              <div className="flex-1 min-w-[240px]">
                <div className="flex items-center gap-2 flex-wrap">
                  <h3 className="font-semibold">{item.userEmail || `Submission #${item.id}`}</h3>
                  <StatusBadge status={item.status || "PENDING"} />
                </div>
                <p className="text-sm text-text-muted mt-0.5">
                  {item.documentType} · {item.contactNumber}
                </p>
                <p className="text-sm text-text-muted">{item.address}</p>
              </div>
              <div className="flex flex-wrap gap-2">
                <Button variant="outline" onClick={() => openFile(item.id)}>View file</Button>
                <Button variant="outline" onClick={() => setStatus(item, "REJECTED")}>Reject</Button>
                <Button onClick={() => setStatus(item, "APPROVED")}>Approve</Button>
              </div>
            </li>
          ))}
        </ul>
      )}

      {data && <Pagination page={data.number ?? page} totalPages={data.totalPages ?? 1} onChange={setPage} />}
    </div>
  );
}
