import React, { useEffect, useState, useCallback } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useDispatch } from "react-redux";
import { ArrowLeft, Car, Home, Loader2 } from "lucide-react";
import { PolicyService } from "../../lib/services";
import { extractErrorMessage } from "../../lib/api";
import ErrorAlert from "../../components/ui/ErrorAlert";
import StatusBadge from "../../components/ui/StatusBadge";
import { Skeleton } from "../../components/ui/Skeleton";
import Button from "../../components/ui/Button";
import { pushToast } from "../../store/slices/toastSlice";
import { formatCurrency } from "../../lib/constants";

export default function PolicyDetail() {
  const { id } = useParams();
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [policy, setPolicy] = useState(null);
  const [error, setError] = useState("");
  const [buying, setBuying] = useState(false);

  const load = useCallback(async () => {
    setPolicy(null);
    setError("");
    try {
      const { data } = await PolicyService.getActive(id);
      setPolicy(data);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load policy."));
    }
  }, [id]);

  useEffect(() => {
    load();
  }, [load]);

  const onPurchase = useCallback(async () => {
    setBuying(true);
    try {
      await PolicyService.purchase({ policyId: Number(id) });
      dispatch(pushToast({ message: "Policy purchased successfully.", variant: "success" }));
      navigate("/app/my-policies", { replace: true });
    } catch (err) {
      dispatch(pushToast({ message: extractErrorMessage(err, "Purchase failed."), variant: "danger" }));
    } finally {
      setBuying(false);
    }
  }, [dispatch, id, navigate]);

  const type = String(policy?.policyType || policy?.type || "").toUpperCase();
  const Icon = type === "VEHICLE" ? Car : Home;

  return (
    <div className="space-y-6">
      <Link
        to="/app/policies"
        className="inline-flex items-center gap-1 text-sm text-text-muted hover:text-text"
      >
        <ArrowLeft size={14} /> Back to policies
      </Link>

      {error && <ErrorAlert message={error} onRetry={load} />}

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
                <Icon size={24} />
              </div>
              <div className="flex-1 min-w-[200px]">
                <div className="flex items-center gap-2 flex-wrap">
                  <StatusBadge status={type || "POLICY"} />
                  <StatusBadge status={policy.status || "ACTIVE"} />
                </div>
                <h1 className="font-heading text-3xl sm:text-4xl font-semibold mt-2">
                  {policy.name || policy.title || `Policy #${policy.id}`}
                </h1>
                <p className="mt-2 text-text-muted max-w-2xl">
                  {policy.description ||
                    "Comprehensive coverage with transparent terms and fast claim processing."}
                </p>
              </div>
              <Button onClick={onPurchase} loading={buying}>
                {buying ? "Processing" : "Purchase policy"}
              </Button>
            </div>

            <div className="mt-8 grid sm:grid-cols-3 gap-4">
              <Stat label="Premium" value={formatCurrency(policy.premium || policy.premiumAmount || 0)} />
              <Stat label="Coverage" value={formatCurrency(policy.coverage || policy.coverageAmount || 0)} />
              <Stat label="Term" value={`${policy.termMonths || policy.duration || 12} months`} />
            </div>
          </div>

          <div className="rounded-2xl border border-border bg-surface p-6">
            <h2 className="font-heading text-xl font-semibold">What's included</h2>
            <ul className="mt-3 grid sm:grid-cols-2 gap-2 text-sm text-text-muted">
              {(policy.benefits || DEFAULT_BENEFITS).map((b, i) => (
                <li key={i} className="flex items-start gap-2">
                  <span className="mt-1.5 w-1.5 h-1.5 rounded-full bg-brand shrink-0" />
                  <span>{b}</span>
                </li>
              ))}
            </ul>
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

const DEFAULT_BENEFITS = [
  "24/7 claims support",
  "Transparent terms with no hidden fees",
  "Fast online filing with status tracking",
  "Trusted partner network",
];

// eslint-disable-next-line no-unused-vars
const _unused = Loader2;
