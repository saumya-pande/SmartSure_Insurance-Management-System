import React, { useEffect, useState, useCallback, memo } from "react";
import { useForm } from "react-hook-form";
import { useDispatch } from "react-redux";
import * as yup from "yup";
import { yupResolver } from "@hookform/resolvers/yup";
import { Plus, Car, Home, Trash2, Pencil, X } from "lucide-react";
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

const schema = yup.object({
  name: yup.string().trim().required("Name required"),
  policyType: yup.string().oneOf(POLICY_TYPES, "Select type").required(),
  premium: yup.number().typeError("Number").positive().required(),
  coverage: yup.number().typeError("Number").positive().required(),
  termMonths: yup.number().typeError("Number").integer().positive().required(),
  description: yup.string().trim().max(1000),
});

export default function AdminPolicies() {
  const dispatch = useDispatch();
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [editing, setEditing] = useState(null); // null | "new" | policyObject

  const load = useCallback(async () => {
    setData(null);
    setError("");
    try {
      const { data: res } = await AdminPolicyService.list({ page, size: PAGE_SIZE });
      setData(res);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load policies."));
      setData({ content: [], totalPages: 1, number: 0 });
    }
  }, [page]);

  useEffect(() => {
    load();
  }, [load]);

  const remove = useCallback(
    async (p) => {
      try {
        await AdminPolicyService.remove(p.id);
        dispatch(pushToast({ message: "Policy deleted.", variant: "success" }));
        load();
      } catch (err) {
        dispatch(pushToast({ message: extractErrorMessage(err), variant: "danger" }));
      }
    },
    [dispatch, load]
  );

  const toggle = useCallback(
    async (p) => {
      const newStatus = String(p.status).toUpperCase() === "ACTIVE" ? "INACTIVE" : "ACTIVE";
      try {
        await AdminPolicyService.setStatus(p.id, newStatus);
        dispatch(pushToast({ message: `Policy ${newStatus.toLowerCase()}.`, variant: "success" }));
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
      <header className="flex items-end justify-between gap-3 flex-wrap">
        <div>
          <p className="text-xs uppercase tracking-widest text-text-subtle">Catalog</p>
          <h1 className="font-heading text-4xl font-semibold mt-1">Policies</h1>
        </div>
        <Button onClick={() => setEditing("new")}>
          <Plus size={16} /> New policy
        </Button>
      </header>

      {error && <ErrorAlert message={error} onRetry={load} />}

      {data === null ? (
        <ListCardSkeleton />
      ) : list.length === 0 ? (
        <EmptyState icon={Plus} title="No policies yet" description="Create the first one." />
      ) : (
        <ul className="space-y-3">
          {list.map((p) => (
            <PolicyRow
              key={p.id}
              policy={p}
              onEdit={setEditing}
              onDelete={remove}
              onToggle={toggle}
            />
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

const PolicyRow = memo(function PolicyRow({ policy, onEdit, onDelete, onToggle }) {
  const type = String(policy.policyType || policy.type || "").toUpperCase();
  const Icon = type === "VEHICLE" ? Car : Home;
  const handleEdit = useCallback(() => onEdit(policy), [onEdit, policy]);
  const handleDelete = useCallback(() => onDelete(policy), [onDelete, policy]);
  const handleToggle = useCallback(() => onToggle(policy), [onToggle, policy]);
  return (
    <li className="rounded-xl border border-border bg-surface p-4 flex flex-wrap items-center gap-4">
      <div className="grid place-items-center w-11 h-11 rounded-md bg-brand-soft text-brand">
        <Icon size={18} />
      </div>
      <div className="flex-1 min-w-[200px]">
        <div className="flex items-center gap-2 flex-wrap">
          <h3 className="font-heading text-lg font-semibold">{policy.name || `Policy #${policy.id}`}</h3>
          <StatusBadge status={type || "POLICY"} />
          <StatusBadge status={policy.status || "ACTIVE"} />
        </div>
        <p className="text-sm text-text-muted mt-0.5">
          {formatCurrency(policy.premium || 0)} premium · {formatCurrency(policy.coverage || 0)} coverage
        </p>
      </div>
      <div className="flex gap-2">
        <Button variant="outline" onClick={handleToggle}>
          {String(policy.status).toUpperCase() === "ACTIVE" ? "Deactivate" : "Activate"}
        </Button>
        <Button variant="ghost" onClick={handleEdit} aria-label="Edit policy">
          <Pencil size={16} />
        </Button>
        <Button variant="ghost" onClick={handleDelete} aria-label="Delete policy" className="text-danger">
          <Trash2 size={16} />
        </Button>
      </div>
    </li>
  );
});

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
    resolver: yupResolver(schema),
    defaultValues: {
      name: initial?.name || "",
      policyType: initial?.policyType || initial?.type || "",
      premium: initial?.premium || "",
      coverage: initial?.coverage || "",
      termMonths: initial?.termMonths || initial?.duration || 12,
      description: initial?.description || "",
    },
  });

  const onSubmit = async (values) => {
    setSubmitting(true);
    setError("");
    try {
      if (isEdit) {
        await AdminPolicyService.update(initial.id, values);
        dispatch(pushToast({ message: "Policy updated.", variant: "success" }));
      } else {
        await AdminPolicyService.create(values);
        dispatch(pushToast({ message: "Policy created.", variant: "success" }));
      }
      onSaved();
    } catch (err) {
      setError(extractErrorMessage(err, "Save failed."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-[80] grid place-items-center px-4">
      <div className="absolute inset-0 bg-black/50" onClick={onClose} aria-hidden="true" />
      <div
        role="dialog"
        aria-modal="true"
        className="relative w-full max-w-lg rounded-2xl bg-surface border border-border shadow-elevated p-6 max-h-[90vh] overflow-y-auto"
      >
        <button
          type="button"
          onClick={onClose}
          className="absolute top-3 right-3 p-1.5 rounded-md text-text-muted hover:bg-surface-2"
          aria-label="Close"
        >
          <X size={16} />
        </button>
        <h2 className="font-heading text-2xl font-semibold">
          {isEdit ? "Edit policy" : "New policy"}
        </h2>
        <form onSubmit={handleSubmit(onSubmit)} className="mt-5 space-y-4" noValidate>
          {error && <ErrorAlert message={error} />}
          <Field id="p-name" label="Name" error={errors.name?.message} {...register("name")} />
          <Field
            as="select"
            id="p-type"
            label="Type"
            error={errors.policyType?.message}
            {...register("policyType")}
          >
            <option value="">Select…</option>
            {POLICY_TYPES.map((t) => (
              <option key={t} value={t}>{t}</option>
            ))}
          </Field>
          <div className="grid grid-cols-2 gap-3">
            <Field id="p-premium" label="Premium" type="number" step="0.01" error={errors.premium?.message} {...register("premium")} />
            <Field id="p-coverage" label="Coverage" type="number" step="0.01" error={errors.coverage?.message} {...register("coverage")} />
          </div>
          <Field id="p-term" label="Term (months)" type="number" error={errors.termMonths?.message} {...register("termMonths")} />
          <Field as="textarea" id="p-desc" label="Description" error={errors.description?.message} {...register("description")} />
          <div className="flex justify-end gap-2 pt-2">
            <Button variant="outline" type="button" onClick={onClose}>Cancel</Button>
            <Button type="submit" loading={submitting}>{isEdit ? "Save changes" : "Create policy"}</Button>
          </div>
        </form>
      </div>
    </div>
  );
}
