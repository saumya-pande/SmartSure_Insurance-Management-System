import React, { useEffect, useState, useCallback } from "react";
import { useForm } from "react-hook-form";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useDispatch } from "react-redux";
import * as yup from "yup";
import { yupResolver } from "@hookform/resolvers/yup";
import { Upload } from "lucide-react";
import Field from "../../components/ui/Field";
import Button from "../../components/ui/Button";
import ErrorAlert from "../../components/ui/ErrorAlert";
import { ClaimService, PolicyService } from "../../lib/services";
import { pushToast } from "../../store/slices/toastSlice";
import { extractErrorMessage } from "../../lib/api";

const schema = yup.object({
  customerPolicyId: yup.number().typeError("Select a policy").required("Select a policy"),
  claimAmount: yup
    .number()
    .typeError("Enter a number")
    .positive("Must be positive")
    .required("Claim amount is required"),
  description: yup.string().trim().max(1000, "Max 1000 characters"),
});

export default function FileClaim() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const presetPolicy = params.get("policy");

  const [policies, setPolicies] = useState([]);
  const [files, setFiles] = useState([]);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(schema),
    defaultValues: {
      customerPolicyId: presetPolicy || "",
      claimAmount: "",
      description: "",
    },
  });

  const loadPolicies = useCallback(async () => {
    try {
      const { data } = await PolicyService.myPurchases();
      setPolicies(Array.isArray(data) ? data : data?.content || []);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load your policies."));
    }
  }, []);

  useEffect(() => {
    loadPolicies();
  }, [loadPolicies]);

  const onFiles = useCallback((e) => {
    setFiles(Array.from(e.target.files || []));
  }, []);

  const onSubmit = async (values) => {
    setSubmitting(true);
    setError("");
    try {
      const fd = new FormData();
      fd.append("customerPolicyId", String(values.customerPolicyId));
      fd.append("claimAmount", String(values.claimAmount));
      if (values.description) fd.append("description", values.description);
      files.forEach((f) => fd.append("files", f));
      await ClaimService.fileClaim(fd);
      dispatch(pushToast({ message: "Claim filed successfully.", variant: "success" }));
      reset();
      setFiles([]);
      navigate("/app/my-policies", { replace: true });
    } catch (err) {
      setError(extractErrorMessage(err, "Failed to file claim."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6 max-w-2xl">
      <header>
        <p className="text-xs uppercase tracking-widest text-text-subtle">Claims</p>
        <h1 className="font-heading text-4xl font-semibold mt-1">File a claim</h1>
        <p className="mt-2 text-text-muted">Submit details and supporting documents.</p>
      </header>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5" noValidate>
        {error && <ErrorAlert message={error} />}

        <Field
          as="select"
          id="customerPolicyId"
          label="Policy"
          error={errors.customerPolicyId?.message}
          {...register("customerPolicyId")}
        >
          <option value="">Select a policy…</option>
          {policies.map((p) => (
            <option key={p.id} value={p.id}>
              {(p.policyName || p.name || `Policy #${p.id}`) +
                ` · ${String(p.policyType || p.type || "").toUpperCase()}`}
            </option>
          ))}
        </Field>

        <Field
          id="claimAmount"
          label="Claim amount (USD)"
          type="number"
          step="0.01"
          min="0"
          error={errors.claimAmount?.message}
          {...register("claimAmount")}
        />

        <Field
          as="textarea"
          id="description"
          label="Description (optional)"
          placeholder="Describe what happened…"
          error={errors.description?.message}
          {...register("description")}
        />

        <div>
          <label htmlFor="files" className="block text-sm font-semibold text-text mb-1.5">
            Evidence files
          </label>
          <label
            htmlFor="files"
            className="flex flex-col items-center justify-center gap-2 px-4 py-8 rounded-xl border border-dashed border-border bg-surface text-center cursor-pointer hover:border-brand hover:bg-surface-2"
          >
            <Upload size={20} className="text-text-muted" />
            <p className="text-sm text-text">
              {files.length > 0
                ? `${files.length} file(s) selected`
                : "Click to select photos, PDFs, or documents"}
            </p>
            <p className="text-xs text-text-subtle">PNG, JPG, PDF up to 10MB each</p>
          </label>
          <input
            id="files"
            type="file"
            multiple
            onChange={onFiles}
            className="sr-only"
            accept="image/*,application/pdf"
          />
        </div>

        <Button type="submit" loading={submitting}>
          Submit claim
        </Button>
      </form>
    </div>
  );
}
