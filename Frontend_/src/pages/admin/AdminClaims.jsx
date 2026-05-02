import React, { useEffect, useState, useCallback } from "react";
import { useDispatch } from "react-redux";
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
import FilePreviewDialog from "../../components/ui/FilePreviewDialog";

const PAGE_SIZE = 10;

export default function AdminClaims() {
  const dispatch = useDispatch();
  const [page, setPage] = useState(0);
  const [filters, setFilters] = useState({ status: "", email: "", startDate: "", endDate: "" });
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [previewFile, setPreviewFile] = useState(null);

  const load = useCallback(async () => {
    setData(null);
    setError("");
    try {
      const params = { page, size: PAGE_SIZE };
      if (filters.status) params.status = filters.status;
      if (filters.email) params.email = filters.email;
      if (filters.startDate) params.startDate = filters.startDate;
      if (filters.endDate) params.endDate = filters.endDate;
      const { data: response } = await AdminClaimService.list(params);
      setData(response);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load claims."));
      setData({ content: [], totalPages: 1, number: 0 });
    }
  }, [filters, page]);

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

  const openDocument = useCallback(
    (doc) => {
      if (!doc.fileUrl) {
        dispatch(pushToast({ message: "No file available for this document.", variant: "danger" }));
        return;
      }
      setPreviewFile({ viewUrl: doc.fileUrl, fileName: doc.fileName || "Claim document" });
    },
    [dispatch]
  );

  const claims = data?.content || [];

  return (
    <div className="space-y-6">
      <header>
        <p className="text-xs uppercase tracking-widest text-text-subtle">Operations</p>
        <h1 className="font-heading text-4xl font-semibold mt-1">Claims</h1>
      </header>

      <section className="rounded-2xl border border-border bg-surface p-4 grid md:grid-cols-4 gap-3">
        <Field as="select" id="claim-status" label="Status" value={filters.status} onChange={(event) => setFilters((current) => ({ ...current, status: event.target.value }))}>
          <option value="">All statuses</option>
          {CLAIM_STATUSES.map((status) => (
            <option key={status} value={status}>{status}</option>
          ))}
        </Field>
        <Field id="claim-email" label="Customer email" value={filters.email} onChange={(event) => setFilters((current) => ({ ...current, email: event.target.value }))} />
        <Field id="claim-start" label="Start date" type="date" value={filters.startDate} onChange={(event) => setFilters((current) => ({ ...current, startDate: event.target.value }))} />
        <Field id="claim-end" label="End date" type="date" value={filters.endDate} onChange={(event) => setFilters((current) => ({ ...current, endDate: event.target.value }))} />
      </section>

      {error && <ErrorAlert message={error} onRetry={load} />}

      {data === null ? (
        <ListCardSkeleton />
      ) : claims.length === 0 ? (
        <EmptyState icon="clipboard" title="No claims" />
      ) : (
        <ul className="space-y-3">
          {claims.map((claim) => (
            <li key={claim.id} className="rounded-xl border border-border bg-surface p-4 flex flex-wrap items-center gap-4">
              <div className="flex-1 min-w-[240px]">
                <div className="flex items-center gap-2 flex-wrap">
                  <h3 className="font-semibold">Claim #{claim.id}</h3>
                  <StatusBadge status={claim.status || "SUBMITTED"} />
                </div>
                <p className="text-sm text-text-muted mt-0.5">
                  {claim.customerEmail || "Unknown"} · Policy #{claim.customerPolicyId}
                </p>
                <p className="text-sm text-text-muted">
                  {formatCurrency(claim.claimAmount || 0)} · {new Date(claim.createdAt).toLocaleString()}
                </p>
              </div>
              <div className="flex flex-wrap gap-2">
                {(claim.documents || []).map((document, index) => (
                  <Button
                    key={document.id || `${claim.id}-${index}`}
                    variant="outline"
                    onClick={() => openDocument(document)}
                  >
                    {document.fileName || `Document ${index + 1}`}
                  </Button>
                ))}
                <Field
                  as="select"
                  id={`override-${claim.id}`}
                  defaultValue=""
                  onChange={(event) => {
                    const value = event.target.value;
                    if (value) override(claim, value);
                    event.target.value = "";
                  }}
                >
                  <option value="">Override status</option>
                  {CLAIM_STATUSES.map((status) => (
                    <option key={status} value={status}>{status}</option>
                  ))}
                </Field>
              </div>
            </li>
          ))}
        </ul>
      )}

      {data && <Pagination page={data.number ?? page} totalPages={data.totalPages ?? 1} onChange={setPage} />}
      <FilePreviewDialog file={previewFile} title="Claim document" onClose={() => setPreviewFile(null)} />
    </div>
  );
}
