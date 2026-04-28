import React, { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { useSelector } from "react-redux";
import { KycService } from "../../lib/services";
import { selectAuth } from "../../store/slices/authSlice";
import { extractErrorMessage } from "../../lib/api";
import { openBlobResponse } from "../../lib/file";
import ErrorAlert from "../../components/ui/ErrorAlert";
import StatusBadge from "../../components/ui/StatusBadge";
import Button from "../../components/ui/Button";
import { Skeleton } from "../../components/ui/Skeleton";
import { formatStatusLabel } from "../../lib/constants";
import Icon from "../../components/ui/Icon";

export default function CustomerProfile() {
  const { user } = useSelector(selectAuth);
  const [kyc, setKyc] = useState(undefined);
  const [error, setError] = useState("");
  const [openingFile, setOpeningFile] = useState(false);

  const load = useCallback(async () => {
    setError("");
    setKyc(undefined);
    try {
      const { data } = await KycService.myStatus();
      setKyc(data);
    } catch (err) {
      if (err?.response?.status === 404) {
        setKyc(null);
      } else {
        setError(extractErrorMessage(err, "Could not load profile details."));
        setKyc(null);
      }
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const displayName = useMemo(() => {
    if (user?.name && String(user.name).trim()) return String(user.name).trim();
    const email = String(user?.email || "").trim();
    if (!email) return "Customer";
    return email
      .split("@")[0]
      .replace(/[._-]+/g, " ")
      .replace(/\b\w/g, (char) => char.toUpperCase());
  }, [user]);

  const openFile = useCallback(async () => {
    setOpeningFile(true);
    setError("");
    try {
      const response = await KycService.myFile();
      openBlobResponse(response);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not open KYC file."));
    } finally {
      setOpeningFile(false);
    }
  }, []);

  return (
    <div className="space-y-6">
      <header>
        <p className="text-xs uppercase tracking-widest text-text-subtle">Account</p>
        <h1 className="font-heading text-4xl font-semibold mt-1">Profile</h1>
        <p className="mt-2 text-text-muted">Your account, contact identity, and KYC details.</p>
      </header>

      {error && <ErrorAlert message={error} onRetry={load} />}

      <section className="grid gap-4 lg:grid-cols-[1fr,1fr]">
        <div className="rounded-2xl border border-border bg-surface p-5">
          <div className="flex items-center gap-3">
            <div className="grid place-items-center w-12 h-12 rounded-xl bg-brand-soft text-brand">
              <Icon name="user" />
            </div>
            <div>
              <p className="text-xs uppercase tracking-widest text-text-subtle">Identity</p>
              <h2 className="font-heading text-2xl font-semibold mt-1">{displayName}</h2>
            </div>
          </div>

          <div className="mt-5 space-y-4">
            <ProfileRow label="Name" value={displayName} />
            <ProfileRow label="Email" value={user?.email || "Not available"} />
            <ProfileRow label="Role" value={formatStatusLabel(user?.role || "CUSTOMER")} />
          </div>
        </div>

        <div className="rounded-2xl border border-border bg-surface p-5">
          <div className="flex items-center justify-between gap-3">
            <div>
              <p className="text-xs uppercase tracking-widest text-text-subtle">Verification</p>
              <h2 className="font-heading text-2xl font-semibold mt-1">KYC details</h2>
            </div>
            {kyc === undefined ? (
              <Skeleton className="h-6 w-24" />
            ) : kyc?.status ? (
              <StatusBadge status={kyc.status} />
            ) : (
              <StatusBadge status="PENDING">Not Submitted</StatusBadge>
            )}
          </div>

          {kyc === undefined ? (
            <div className="mt-5 space-y-3">
              <Skeleton className="h-5 w-full" />
              <Skeleton className="h-5 w-5/6" />
              <Skeleton className="h-5 w-2/3" />
            </div>
          ) : kyc ? (
            <div className="mt-5 space-y-4">
              <ProfileRow label="Contact number" value={kyc.contactNumber || "Not available"} />
              <ProfileRow label="Address" value={kyc.address || "Not available"} />
              <ProfileRow label="Document type" value={formatStatusLabel(kyc.documentType || "Unknown")} />
              <ProfileRow label="Status" value={formatStatusLabel(kyc.status)} />
              <div className="pt-2">
                <Button type="button" variant="outline" onClick={openFile} loading={openingFile}>
                  View uploaded file
                </Button>
              </div>
            </div>
          ) : (
            <div className="mt-5 rounded-xl bg-surface-2 p-4 text-sm text-text-muted space-y-3">
              <p>KYC not submitted yet.</p>
              <Link to="/app/kyc" className="inline-flex items-center gap-2 font-semibold text-brand hover:underline">
                <Icon name="shield" />
                Complete KYC
              </Link>
            </div>
          )}
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-3">
        <QuickLink to="/app" icon="clipboard" title="Dashboard" body="Check live policy and claim activity." />
        <QuickLink to="/app/policies" icon="folder-open" title="Bazaar" body="Browse live home and vehicle plans." />
        <QuickLink to="/app/my-policies" icon="file" title="My Policies" body="Review purchased coverage and file claims." />
      </section>
    </div>
  );
}

function ProfileRow({ label, value }) {
  return (
    <div className="rounded-xl bg-surface-2 px-4 py-3">
      <p className="text-xs uppercase tracking-widest text-text-subtle">{label}</p>
      <p className="mt-1 font-medium break-words">{value}</p>
    </div>
  );
}

function QuickLink({ to, icon, title, body }) {
  return (
    <Link
      to={to}
      className="rounded-2xl border border-border bg-surface p-5 hover:border-brand transition-colors"
    >
      <div className="grid place-items-center w-10 h-10 rounded-xl bg-brand-soft text-brand">
        <Icon name={icon} />
      </div>
      <h3 className="font-heading text-xl font-semibold mt-4">{title}</h3>
      <p className="mt-2 text-sm text-text-muted">{body}</p>
    </Link>
  );
}
