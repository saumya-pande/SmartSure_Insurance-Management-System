import React, { useEffect, useState, useCallback } from "react";
import { useForm } from "react-hook-form";
import { useDispatch } from "react-redux";
import { Link } from "react-router-dom";
import { ClaimService } from "../../lib/services";
import { extractErrorMessage } from "../../lib/api";
import ErrorAlert from "../../components/ui/ErrorAlert";
import StatusBadge from "../../components/ui/StatusBadge";
import { ListCardSkeleton } from "../../components/ui/Skeleton";
import EmptyState from "../../components/ui/EmptyState";
import Pagination from "../../components/ui/Pagination";
import Button from "../../components/ui/Button";
import Field from "../../components/ui/Field";
import { pushToast } from "../../store/slices/toastSlice";
import { formatCurrency } from "../../lib/constants";
import Icon from "../../components/ui/Icon";

const PAGE_SIZE = 8;

export default function MyClaims() {
  const dispatch = useDispatch();
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [editing, setEditing] = useState(null);

  const load = useCallback(async () => {
    setData(null);
    setError("");
    try {
      const { data: response } = await ClaimService.myClaims({ page, size: PAGE_SIZE });
      setData(response);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load your claims."));
      setData({ content: [], totalPages: 1, number: 0 });
    }
  }, [page]);

  useEffect(() => {
    load();
  }, [load]);

  const submitDraft = useCallback(
    async (claim) => {
      try {
        await ClaimService.submit(claim.id);
        dispatch(pushToast({ message: "Claim submitted.", variant: "success" }));
        load();
      } catch (err) {
        dispatch(pushToast({ message: extractErrorMessage(err, "Could not submit claim."), variant: "danger" }));
      }
    },
    [dispatch, load]
  );

  const claims = data?.content || [];

  return (
    <div className="space-y-6">
      <header className="flex items-end justify-between gap-3 flex-wrap">
        <div>
          <p className="text-xs uppercase tracking-widest text-text-subtle">Claims</p>
          <h1 className="font-heading text-4xl font-semibold mt-1">My claims</h1>
          <p className="mt-2 text-text-muted">Draft, submit, and track every claim status.</p>
        </div>
        <Link
          to="/app/claims/new"
          className="hidden sm:inline-flex items-center gap-2 px-4 py-2 rounded-md bg-brand text-white font-semibold hover:bg-brand-hover transition-colors"
        >
          <Icon name="claims" />
          File claim
        </Link>
      </header>

      <div className="sm:hidden grid grid-cols-2 gap-3">
        <div className="rounded-2xl border border-brand bg-brand-soft px-4 py-3">
          <p className="text-xs uppercase tracking-widest text-brand">Current claims</p>
          <p className="mt-1 font-semibold text-text">View drafts and live statuses</p>
        </div>
        <Link
          to="/app/claims/new"
          className="rounded-2xl border border-border bg-surface px-4 py-3 hover:border-brand transition-colors"
        >
          <p className="text-xs uppercase tracking-widest text-text-subtle">Draft or file</p>
          <p className="mt-1 font-semibold text-text">Create a new claim</p>
        </Link>
      </div>

      {error && <ErrorAlert message={error} onRetry={load} />}

      {data === null ? (
        <ListCardSkeleton />
      ) : claims.length === 0 ? (
        <EmptyState
          icon="claims"
          title="No claims yet"
          description="Create a draft claim from a purchased policy."
          action={
            <Link
              to="/app/claims/new"
              className="inline-flex items-center gap-2 px-4 py-2 rounded-md bg-brand text-white font-semibold hover:bg-brand-hover transition-colors"
            >
              <Icon name="claims" />
              File claim
            </Link>
          }
        />
      ) : (
        <ul className="space-y-3">
          {claims.map((claim) => (
            <li key={claim.id} className="rounded-xl border border-border bg-surface p-4 flex flex-wrap items-center gap-4">
              <div className="flex-1 min-w-[220px]">
                <div className="flex items-center gap-2 flex-wrap">
                  <h3 className="font-heading text-lg font-semibold">Claim #{claim.id}</h3>
                  <StatusBadge status={claim.status} />
                </div>
                <p className="text-sm text-text-muted mt-1">
                  Policy #{claim.customerPolicyId} · {formatCurrency(claim.claimAmount)}
                </p>
                <p className="text-sm text-text-muted">
                  Created {new Date(claim.createdAt).toLocaleString()}
                </p>
              </div>
              {String(claim.status).toUpperCase() === "DRAFT" && (
                <div className="flex flex-wrap gap-2">
                  <Button variant="outline" onClick={() => setEditing(claim)}>
                    Edit draft
                  </Button>
                  <Button onClick={() => submitDraft(claim)}>Submit</Button>
                </div>
              )}
            </li>
          ))}
        </ul>
      )}

      {data && (
        <Pagination page={data.number ?? page} totalPages={data.totalPages ?? 1} onChange={setPage} />
      )}

      {editing && (
        <EditDraftDialog
          claim={editing}
          onClose={() => setEditing(null)}
          onSaved={() => {
            setEditing(null);
            load();
          }}
        />
      )}
    </div>
  );
}

function EditDraftDialog({ claim, onClose, onSaved }) {
  const dispatch = useDispatch();
  const [error, setError] = useState("");
  const [files, setFiles] = useState([]);
  const [submitting, setSubmitting] = useState("");
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    defaultValues: { claimAmount: claim.claimAmount || "" },
  });

  const save = useCallback(
    async (values, mode) => {
      setSubmitting(mode);
      setError("");
      try {
        const formData = new FormData();
        formData.append("claimAmount", String(values.claimAmount));
        files.forEach((file) => formData.append("files", file));
        await ClaimService.updateDraft(claim.id, formData);
        if (mode === "submit") {
          await ClaimService.submit(claim.id);
          dispatch(pushToast({ message: "Draft updated and submitted.", variant: "success" }));
        } else {
          dispatch(pushToast({ message: "Draft updated.", variant: "success" }));
        }
        onSaved();
      } catch (err) {
        setError(extractErrorMessage(err, "Could not update draft."));
      } finally {
        setSubmitting("");
      }
    },
    [claim.id, dispatch, files, onSaved]
  );

  return (
    <div className="fixed inset-0 z-[80] grid place-items-center px-4">
      <div className="absolute inset-0 bg-black/50" onClick={onClose} aria-hidden="true" />
      <div className="relative w-full max-w-lg rounded-2xl bg-surface border border-border shadow-elevated p-6">
        <h2 className="font-heading text-2xl font-semibold">Edit claim draft</h2>
        <form onSubmit={handleSubmit((values) => save(values, "draft"))} className="mt-5 space-y-4" noValidate>
          {error && <ErrorAlert message={error} />}
          <Field
            id="claimAmount"
            label="Claim amount"
            type="number"
            step="0.01"
            error={errors.claimAmount?.message}
            {...register("claimAmount", { required: "Claim amount is required" })}
          />
          <div>
            <label htmlFor="edit-claim-files" className="block text-sm font-semibold text-text mb-1.5">
              Add more files
            </label>
            <input
              id="edit-claim-files"
              type="file"
              multiple
              onChange={(event) => setFiles(Array.from(event.target.files || []))}
              accept="image/*,application/pdf"
              className="w-full text-sm"
            />
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <Button variant="outline" type="button" onClick={onClose}>
              Cancel
            </Button>
            <Button variant="outline" type="button" loading={submitting === "submit"} onClick={handleSubmit((values) => save(values, "submit"))}>
              Save and submit
            </Button>
            <Button type="submit" loading={submitting === "draft"}>
              Save draft
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
