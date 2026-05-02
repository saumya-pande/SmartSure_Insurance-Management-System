import React, { useEffect, useState, useCallback } from "react";
import { useForm } from "react-hook-form";
import { useDispatch } from "react-redux";
import { AdminPolicyService } from "../../lib/services";
import { extractErrorMessage } from "../../lib/api";
import ErrorAlert from "../../components/ui/ErrorAlert";
import StatusBadge from "../../components/ui/StatusBadge";
import { ListCardSkeleton } from "../../components/ui/Skeleton";
import EmptyState from "../../components/ui/EmptyState";
import Pagination from "../../components/ui/Pagination";
import Button from "../../components/ui/Button";
import Field from "../../components/ui/Field";
import { pushToast } from "../../store/slices/toastSlice";
import { formatCurrency, POLICY_TYPES } from "../../lib/constants";

const PAGE_SIZE = 10;

export default function AdminPolicies() {
  const dispatch = useDispatch();
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [editing, setEditing] = useState(null);
  const [filters, setFilters] = useState({ policyName: "", type: "", status: "" });

  const load = useCallback(async () => {
    setData(null);
    setError("");
    try {
      const params = { page, size: PAGE_SIZE };
      if (filters.policyName) params.policyName = filters.policyName;
      if (filters.type) params.type = filters.type;
      if (filters.status) params.status = filters.status;
      const { data: response } = await AdminPolicyService.list(params);
      setData(response);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load policies."));
      setData({ content: [], totalPages: 1, number: 0 });
    }
  }, [filters, page]);

  useEffect(() => {
    load();
  }, [load]);

  const remove = useCallback(
    async (policy) => {
      try {
        await AdminPolicyService.remove(policy.id);
        dispatch(pushToast({ message: "Policy deleted.", variant: "success" }));
        load();
      } catch (err) {
        dispatch(pushToast({ message: extractErrorMessage(err), variant: "danger" }));
      }
    },
    [dispatch, load]
  );

  const toggle = useCallback(
    async (policy) => {
      const nextStatus = String(policy.status).toUpperCase() === "ACTIVE" ? "INACTIVE" : "ACTIVE";
      try {
        await AdminPolicyService.setStatus(policy.id, nextStatus);
        dispatch(pushToast({ message: `Policy ${nextStatus.toLowerCase()}.`, variant: "success" }));
        load();
      } catch (err) {
        dispatch(pushToast({ message: extractErrorMessage(err), variant: "danger" }));
      }
    },
    [dispatch, load]
  );

  const policies = data?.content || [];

  return (
    <div className="space-y-6">
      <header className="flex items-end justify-between gap-3 flex-wrap">
        <div>
          <p className="text-xs uppercase tracking-widest text-text-subtle">Catalog</p>
          <h1 className="font-heading text-4xl font-semibold mt-1">Policies</h1>
        </div>
        <Button onClick={() => setEditing("new")}>New policy</Button>
      </header>

      <section className="rounded-2xl border border-border bg-surface p-4 grid md:grid-cols-3 gap-3">
        <Field id="policy-name" label="Policy name" value={filters.policyName} onChange={(event) => setFilters((current) => ({ ...current, policyName: event.target.value }))} />
        <Field as="select" id="policy-type" label="Type" value={filters.type} onChange={(event) => setFilters((current) => ({ ...current, type: event.target.value }))}>
          <option value="">All types</option>
          {POLICY_TYPES.map((type) => (
            <option key={type} value={type}>{type}</option>
          ))}
        </Field>
        <Field as="select" id="policy-status" label="Status" value={filters.status} onChange={(event) => setFilters((current) => ({ ...current, status: event.target.value }))}>
          <option value="">All statuses</option>
          <option value="ACTIVE">ACTIVE</option>
          <option value="INACTIVE">INACTIVE</option>
        </Field>
      </section>

      {error && <ErrorAlert message={error} onRetry={load} />}

      {data === null ? (
        <ListCardSkeleton />
      ) : policies.length === 0 ? (
        <EmptyState icon="file" title="No policies yet" description="Create first policy." />
      ) : (
        <ul className="space-y-3">
          {policies.map((policy) => (
            <li key={policy.id} className="rounded-xl border border-border bg-surface p-4 flex flex-wrap items-center gap-4">
              <div className="flex-1 min-w-[220px]">
                <div className="flex items-center gap-2 flex-wrap">
                  <h3 className="font-heading text-lg font-semibold">{policy.policyName}</h3>
                  <StatusBadge status={policy.type} />
                  <StatusBadge status={policy.status} />
                </div>
                <p className="text-sm text-text-muted mt-1">
                  Base premium {formatCurrency(policy.basePremium)} · Max premium {formatCurrency(policy.maxPremium)}
                </p>
              </div>
              <div className="flex gap-2">
                <Button variant="outline" onClick={() => toggle(policy)}>
                  {String(policy.status).toUpperCase() === "ACTIVE" ? "Deactivate" : "Activate"}
                </Button>
                <Button variant="ghost" onClick={() => setEditing(policy)}>Edit</Button>
                <Button variant="ghost" className="text-danger" onClick={() => remove(policy)}>
                  Delete
                </Button>
              </div>
            </li>
          ))}
        </ul>
      )}

      {data && <Pagination page={data.number ?? page} totalPages={data.totalPages ?? 1} onChange={setPage} />}

      {editing && (
        <PolicyDialog
          initial={editing === "new" ? null : editing}
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

function PolicyDialog({ initial, onClose, onSaved }) {
  const dispatch = useDispatch();
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const isEdit = Boolean(initial?.id);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    defaultValues: {
      policyName: initial?.policyName || "",
      type: initial?.type || "",
      basePremium: initial?.basePremium || "",
      maxPremium: initial?.maxPremium || "",
    },
  });

  const onSubmit = useCallback(
    async (values) => {
      setSubmitting(true);
      setError("");
      try {
        const payload = {
          policyName: values.policyName,
          type: values.type,
          basePremium: Number(values.basePremium),
          maxPremium: Number(values.maxPremium),
        };
        if (isEdit) {
          await AdminPolicyService.update(initial.id, payload);
          dispatch(pushToast({ message: "Policy updated.", variant: "success" }));
        } else {
          await AdminPolicyService.create(payload);
          dispatch(pushToast({ message: "Policy created.", variant: "success" }));
        }
        onSaved();
      } catch (err) {
        setError(extractErrorMessage(err, "Save failed."));
      } finally {
        setSubmitting(false);
      }
    },
    [dispatch, initial?.id, isEdit, onSaved]
  );

  return (
    <div className="fixed inset-0 z-[80] grid place-items-center px-4">
      <div className="absolute inset-0 bg-black/50" onClick={onClose} aria-hidden="true" />
      <div className="relative w-full max-w-lg rounded-2xl bg-surface border border-border shadow-elevated p-6 max-h-[90vh] overflow-y-auto">
        <h2 className="font-heading text-2xl font-semibold">{isEdit ? "Edit policy" : "New policy"}</h2>
        <form onSubmit={handleSubmit(onSubmit)} className="mt-5 space-y-4" noValidate>
          {error && <ErrorAlert message={error} />}
          <Field id="policyName" label="Policy name" error={errors.policyName?.message} {...register("policyName", { required: "Policy name is required" })} />
          <Field as="select" id="type" label="Type" error={errors.type?.message} {...register("type", { required: "Type is required" })}>
            <option value="">Select</option>
            {POLICY_TYPES.map((type) => (
              <option key={type} value={type}>{type}</option>
            ))}
          </Field>
          <div className="grid grid-cols-2 gap-3">
            <Field id="basePremium" label="Base premium" type="number" step="0.01" error={errors.basePremium?.message} {...register("basePremium", { required: "Base premium is required" })} />
            <Field id="maxPremium" label="Max premium" type="number" step="0.01" error={errors.maxPremium?.message} {...register("maxPremium", { required: "Max premium is required" })} />
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <Button variant="outline" type="button" onClick={onClose}>Cancel</Button>
            <Button type="submit" loading={submitting}>{isEdit ? "Save changes" : "Create policy"}</Button>
          </div>
        </form>
      </div>
    </div>
  );
}
