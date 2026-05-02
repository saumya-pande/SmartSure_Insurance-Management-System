import React, { useEffect, useState, useCallback } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { useForm } from "react-hook-form";
import { selectAuth } from "../../store/slices/authSlice";
import { PolicyService, KycService } from "../../lib/services";
import { extractErrorMessage } from "../../lib/api";
import ErrorAlert from "../../components/ui/ErrorAlert";
import StatusBadge from "../../components/ui/StatusBadge";
import { Skeleton } from "../../components/ui/Skeleton";
import Button from "../../components/ui/Button";
import Field from "../../components/ui/Field";
import { pushToast } from "../../store/slices/toastSlice";
import { formatCurrency } from "../../lib/constants";
import Icon from "../../components/ui/Icon";

export default function PolicyDetail() {
  const { id } = useParams();
  const { user } = useSelector(selectAuth);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [policy, setPolicy] = useState(null);
  const [error, setError] = useState("");
  const [kycStatus, setKycStatus] = useState(null);
  const [buying, setBuying] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    getValues,
    formState: { errors },
  } = useForm({
    defaultValues: {
      holderName: user?.name || "",
      premiumAmount: "",
      startDate: new Date().toISOString().slice(0, 10),
      endDate: "",
      propertyIdentifier: "",
    },
  });

  const load = useCallback(async () => {
    setPolicy(null);
    setError("");
    try {
      const [{ data: policyData }, { data: kycData }] = await Promise.all([
        PolicyService.getActive(id),
        KycService.myStatus().catch(() => ({ data: null })),
      ]);
      setPolicy(policyData);
      setKycStatus(kycData?.status);
      reset((current) => ({
        ...current,
        premiumAmount: policyData?.basePremium || "",
      }));
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load policy."));
    }
  }, [id, reset]);

  useEffect(() => {
    load();
  }, [load]);

  const onPurchase = useCallback(
    async (values) => {
      setBuying(true);
      setError("");
      try {
        await PolicyService.purchase({
          basicPolicyId: Number(id),
          holderName: values.holderName,
          premiumAmount: Number(values.premiumAmount),
          startDate: values.startDate,
          endDate: values.endDate,
          propertyIdentifier: values.propertyIdentifier,
        });
        dispatch(pushToast({ message: "Policy purchased successfully.", variant: "success" }));
        navigate("/app/my-policies", { replace: true });
      } catch (err) {
        setError(extractErrorMessage(err, "Purchase failed."));
      } finally {
        setBuying(false);
      }
    },
    [dispatch, id, navigate]
  );

  const type = String(policy?.type || policy?.policyType || "").toUpperCase();

  return (
    <div className="space-y-6">
      <Link to="/app/policies" className="inline-flex items-center gap-1 text-sm text-text-muted hover:text-text">
        <Icon name="arrow-left" /> Back to policies
      </Link>

      {error && <ErrorAlert message={error} onRetry={!policy ? load : undefined} />}

      {!policy ? (
        <div className="space-y-3">
          <Skeleton className="h-10 w-2/3" />
          <Skeleton className="h-5 w-1/2" />
          <Skeleton className="h-48 w-full mt-6" />
        </div>
      ) : (
        <>
          <div className="rounded-2xl border border-border bg-surface p-6 sm:p-8">
            <div className="flex items-start gap-4 flex-wrap">
              <div className="grid place-items-center w-14 h-14 rounded-lg bg-brand-soft text-brand">
                <Icon name={type === "VEHICLE" ? "vehicle" : "home"} size="xl" />
              </div>
              <div className="flex-1 min-w-[200px]">
                <div className="flex items-center gap-2 flex-wrap">
                  <StatusBadge status={type || "POLICY"} />
                  <StatusBadge status={policy.status || "ACTIVE"} />
                </div>
                <h1 className="font-heading text-3xl sm:text-4xl font-semibold mt-2">
                  {policy.policyName || `Policy #${policy.id}`}
                </h1>
                <p className="mt-2 text-text-muted max-w-2xl">
                  {policy.description || `Business policy range from ${formatCurrency(policy.basePremium)} to ${formatCurrency(policy.maxPremium)} depending on selected risk details.`}
                </p>
              </div>
            </div>

            <div className="mt-8 grid sm:grid-cols-3 md:grid-cols-5 gap-4">
              <Stat label="Base premium" value={formatCurrency(policy.basePremium || 0)} />
              <Stat label="Max premium" value={formatCurrency(policy.maxPremium || 0)} />
              <Stat label="Min Coverage" value={formatCurrency(policy.minCoverageAmount || 0)} />
              <Stat label="Max Coverage" value={formatCurrency(policy.maxCoverageAmount || 0)} />
              <Stat label="Max Duration" value={`${policy.maxMonthCoverage} months`} />
            </div>
          </div>

          <div className="grid lg:grid-cols-[1.1fr,0.9fr] gap-4">
            <div className="rounded-2xl border border-border bg-surface p-6">
              <h2 className="font-heading text-xl font-semibold">Coverage notes</h2>
              <ul className="mt-3 grid sm:grid-cols-2 gap-2 text-sm text-text-muted">
                {(type === "VEHICLE" ? VEHICLE_NOTES : HOME_NOTES).map((note) => (
                  <li key={note} className="flex items-start gap-2">
                    <span className="mt-1.5 w-1.5 h-1.5 rounded-full bg-brand shrink-0" />
                    <span>{note}</span>
                  </li>
                ))}
              </ul>
            </div>

            <form onSubmit={handleSubmit(onPurchase)} className="rounded-2xl border border-border bg-surface p-6 space-y-4" noValidate>
              <div>
                <p className="text-xs uppercase tracking-widest text-text-subtle">Purchase policy</p>
                <h2 className="font-heading text-2xl font-semibold mt-1">Enter coverage details</h2>
              </div>
 
              {kycStatus !== "APPROVED" && (
                <div className="p-4 rounded-xl bg-amber-50 border border-amber-200 text-amber-800 text-sm flex gap-3">
                  <Icon name="info" className="shrink-0 mt-0.5" />
                  <div>
                    <p className="font-semibold">KYC Verification Required</p>
                    <p className="mt-0.5">Your KYC must be approved by an administrator before you can purchase policies. Current status: <span className="font-bold">{kycStatus || "NOT SUBMITTED"}</span></p>
                  </div>
                </div>
              )}
              <Field
                id="holderName"
                label="Holder name"
                error={errors.holderName?.message}
                {...register("holderName", { required: "Holder name is required" })}
              />
              <Field
                id="premiumAmount"
                label="Premium amount"
                type="number"
                min={policy.basePremium || 0}
                max={policy.maxPremium || undefined}
                step="0.01"
                hint={`Allowed range: ${formatCurrency(policy.basePremium)} to ${formatCurrency(policy.maxPremium)} (billed ${policy.billingCycle?.toLowerCase()})`}
                error={errors.premiumAmount?.message}
                {...register("premiumAmount", { required: "Premium amount is required" })}
              />
              <div className="grid sm:grid-cols-2 gap-3">
                <Field
                  id="startDate"
                  label="Start date"
                  type="date"
                  error={errors.startDate?.message}
                  {...register("startDate", { required: "Start date is required" })}
                />
                <Field
                  id="endDate"
                  label="End date"
                  type="date"
                  error={errors.endDate?.message}
                  {...register("endDate", {
                    required: "End date is required",
                    validate: (value) => {
                      const startStr = getValues("startDate");
                      if (!startStr || !value) return true;
                      const start = new Date(startStr);
                      const end = new Date(value);
                      if (end <= start) return "End date must be after start date";
                      const months = (end.getFullYear() - start.getFullYear()) * 12 + (end.getMonth() - start.getMonth());
                      if (months > (policy.maxMonthCoverage || 0)) {
                        return `Duration cannot exceed ${policy.maxMonthCoverage} months`;
                      }
                      return true;
                    },
                  })}
                />
              </div>
              <Field
                id="propertyIdentifier"
                label={type === "VEHICLE" ? "Vehicle number" : "House No. / Property Identifier"}
                error={errors.propertyIdentifier?.message}
                {...register("propertyIdentifier", { required: "Property identifier is required" })}
              />
              <Button type="submit" loading={buying} className="w-full" disabled={kycStatus !== "APPROVED"}>
                {kycStatus === "APPROVED" ? "Purchase policy" : "Complete KYC to Purchase"}
              </Button>
            </form>
          </div>
        </>
      )}
    </div>
  );
}

function Stat({ label, value }) {
  return (
    <div className="rounded-xl bg-surface-2 p-4">
      <p className="text-xs uppercase tracking-widest text-text-subtle">{label}</p>
      <p className="font-heading text-2xl font-semibold mt-1">{value}</p>
    </div>
  );
}

const VEHICLE_NOTES = [
  "Vehicle identifier required for purchase and later claim verification.",
  "Premium can vary across approved risk band.",
  "Customer can file claims after KYC approval.",
  "Status updates appear in claims workspace.",
];

const HOME_NOTES = [
  "Property identifier required for purchase and record linking.",
  "Premium can vary across approved risk band.",
  "Customer can file claims after KYC approval.",
  "Status updates appear in claims workspace.",
];
