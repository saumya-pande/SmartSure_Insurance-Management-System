import React, { useEffect, useState, useCallback } from "react";
import { useForm } from "react-hook-form";
import { useNavigate, useSearchParams, Link } from "react-router-dom";
import { useDispatch } from "react-redux";
import Field from "../../components/ui/Field";
import Button from "../../components/ui/Button";
import ErrorAlert from "../../components/ui/ErrorAlert";
import { ClaimService, PolicyService } from "../../lib/services";
import { pushToast } from "../../store/slices/toastSlice";
import { extractErrorMessage } from "../../lib/api";
import Icon from "../../components/ui/Icon";

export default function FileClaim() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const presetPolicy = params.get("policy");

  const [policies, setPolicies] = useState([]);
  const [files, setFiles] = useState([]);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState("");

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm({
    defaultValues: {
      customerPolicyId: presetPolicy || "",
      claimAmount: "",
    },
  });
 
  const selectedPolicyId = watch("customerPolicyId");
  const selectedPolicy = policies.find((p) => String(p.id) === String(selectedPolicyId));
  const maxCoverage = selectedPolicy?.coverageAmount || 0;
  
  const hasNotStarted = selectedPolicy?.startDate && new Date(selectedPolicy.startDate) > new Date();
  const isExpired = selectedPolicy?.endDate && new Date(selectedPolicy.endDate) < new Date();
 
  useEffect(() => {
    if (presetPolicy) {
      setValue("customerPolicyId", presetPolicy);
    }
  }, [presetPolicy, setValue]);

  const loadPolicies = useCallback(async () => {
    try {
      const { data } = await PolicyService.myPurchases({ page: 0, size: 100 });
      const content = data?.content || [];
      setPolicies(content);
      if (presetPolicy && content.some(p => String(p.id) === String(presetPolicy))) {
        setValue("customerPolicyId", presetPolicy);
      }
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load your policies."));
    }
  }, [presetPolicy, setValue]);

  useEffect(() => {
    loadPolicies();
  }, [loadPolicies]);

  const submitClaim = useCallback(
    async (values, mode) => {
      if (files.length === 0) {
        setError("Attach at least one evidence file.");
        return;
      }
      setSubmitting(mode);
      setError("");
      try {
        const formData = new FormData();
        formData.append("customerPolicyId", String(values.customerPolicyId));
        formData.append("claimAmount", String(values.claimAmount));
        files.forEach((file) => formData.append("files", file));
        const { data } = await ClaimService.createDraft(formData);
        if (mode === "submit") {
          await ClaimService.submit(data.id);
          dispatch(pushToast({ message: "Claim submitted successfully.", variant: "success" }));
        } else {
          dispatch(pushToast({ message: "Claim draft saved.", variant: "success" }));
        }
        navigate("/app/claims", { replace: true });
      } catch (err) {
        setError(extractErrorMessage(err, "Failed to save claim."));
      } finally {
        setSubmitting("");
      }
    },
    [dispatch, files, navigate]
  );

  return (
    <div className="space-y-6 max-w-2xl">
      <header className="flex items-start justify-between gap-3 flex-wrap">
        <div>
          <p className="text-xs uppercase tracking-widest text-text-subtle">Claims</p>
          <h1 className="font-heading text-4xl font-semibold mt-1">Create claim draft</h1>
          <p className="mt-2 text-text-muted">Save draft first or submit immediately after draft creation.</p>
        </div>
        <Link to="/app/claims" className="text-sm font-semibold text-brand hover:underline">
          View my claims
        </Link>
      </header>

      <div className="sm:hidden grid grid-cols-2 gap-3">
        <Link
          to="/app/claims"
          className="rounded-2xl border border-border bg-surface px-4 py-3 hover:border-brand transition-colors"
        >
          <p className="text-xs uppercase tracking-widest text-text-subtle">Current claims</p>
          <p className="mt-1 font-semibold text-text">See drafts and statuses</p>
        </Link>
        <div className="rounded-2xl border border-brand bg-brand-soft px-4 py-3">
          <p className="text-xs uppercase tracking-widest text-brand">Draft or file</p>
          <p className="mt-1 font-semibold text-text">Upload evidence and save</p>
        </div>
      </div>

      <form onSubmit={handleSubmit((values) => submitClaim(values, "draft"))} className="space-y-5" noValidate>
        {error && <ErrorAlert message={error} />}
 
        {hasNotStarted && (
          <div className="p-4 rounded-xl bg-amber-50 border border-amber-200 text-amber-800 text-sm flex gap-3">
            <Icon name="info" className="shrink-0 mt-0.5" />
            <div>
              <p className="font-semibold">Policy Not Started</p>
              <p className="mt-0.5">You cannot file a claim for this policy until it officially starts on <span className="font-bold">{new Date(selectedPolicy.startDate).toLocaleDateString()}</span>.</p>
            </div>
          </div>
        )}
 
        {isExpired && (
          <div className="p-4 rounded-xl bg-red-50 border border-red-200 text-red-800 text-sm flex gap-3">
            <Icon name="info" className="shrink-0 mt-0.5" />
            <div>
              <p className="font-semibold">Policy Expired</p>
              <p className="mt-0.5">This policy expired on <span className="font-bold">{new Date(selectedPolicy.endDate).toLocaleDateString()}</span>. Claims are no longer permitted.</p>
            </div>
          </div>
        )}

        <Field
          as="select"
          id="customerPolicyId"
          label="Policy"
          error={errors.customerPolicyId?.message}
          {...register("customerPolicyId", { required: "Select a policy" })}
        >
          <option value="">Select a policy</option>
          {policies.map((policy) => (
            <option key={policy.id} value={policy.id}>
              {policy.policyName} · {policy.policyType}
            </option>
          ))}
        </Field>

        <Field
          id="claimAmount"
          label="Claim amount"
          type="number"
          step="0.01"
          min="0"
          max={maxCoverage || undefined}
          placeholder={maxCoverage ? `Max ₹${maxCoverage.toLocaleString()}` : "Enter amount"}
          hint={maxCoverage ? `Your policy coverage limit is ₹${maxCoverage.toLocaleString()}` : "Select a policy to see coverage limit"}
          error={errors.claimAmount?.message}
          {...register("claimAmount", { 
            required: "Claim amount is required",
            validate: (value) => !maxCoverage || Number(value) <= maxCoverage || `Cannot exceed coverage limit of ₹${maxCoverage.toLocaleString()}`
          })}
        />

        <div>
          <label htmlFor="files" className="block text-sm font-semibold text-text mb-1.5">
            Evidence files
          </label>
          <label
            htmlFor="files"
            className="flex flex-col items-center justify-center gap-2 px-4 py-8 rounded-xl border border-dashed border-border bg-surface text-center cursor-pointer hover:border-brand hover:bg-surface-2"
          >
            <span className="grid place-items-center w-12 h-12 rounded-full bg-brand-soft text-brand">
              <Icon name="image" />
            </span>
            <span className="text-text-muted">Select photos, PDFs, or documents</span>
            <p className="text-sm text-text">{files.length > 0 ? `${files.length} file(s) selected` : "Click to upload files"}</p>
          </label>
          <input
            id="files"
            type="file"
            multiple
            onChange={(event) => setFiles(Array.from(event.target.files || []))}
            className="sr-only"
            accept="image/*,application/pdf"
          />
        </div>

        <div className="flex flex-wrap gap-3">
          <Button type="submit" loading={submitting === "draft"} disabled={hasNotStarted || isExpired}>
            Save draft
          </Button>
          <Button
            type="button"
            variant="outline"
            loading={submitting === "submit"}
            onClick={handleSubmit((values) => submitClaim(values, "submit"))}
            disabled={hasNotStarted || isExpired}
          >
            Save and submit
          </Button>
        </div>
      </form>
    </div>
  );
}
