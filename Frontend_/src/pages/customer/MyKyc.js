import React, { useEffect, useState, useCallback } from "react";
import { useForm } from "react-hook-form";
import { useDispatch, useSelector } from "react-redux";
import * as yup from "yup";
import { yupResolver } from "@hookform/resolvers/yup";
import { Upload, ShieldCheck } from "lucide-react";
import Field from "../../components/ui/Field";
import Button from "../../components/ui/Button";
import ErrorAlert from "../../components/ui/ErrorAlert";
import StatusBadge from "../../components/ui/StatusBadge";
import { Skeleton } from "../../components/ui/Skeleton";
import { KycService } from "../../lib/services";
import { selectAuth } from "../../store/slices/authSlice";
import { pushToast } from "../../store/slices/toastSlice";
import { extractErrorMessage } from "../../lib/api";

const schema = yup.object({
  contactNumber: yup.string().trim().min(7, "Too short").required("Required"),
  address: yup.string().trim().min(5, "Too short").required("Required"),
  documentType: yup.string().trim().required("Select a document type"),
});

export default function MyKyc() {
  const dispatch = useDispatch();
  const { user } = useSelector(selectAuth);
  const [status, setStatus] = useState(undefined);
  const [error, setError] = useState("");
  const [file, setFile] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(schema),
    defaultValues: { contactNumber: "", address: "", documentType: "" },
  });

  const load = useCallback(async () => {
    setStatus(undefined);
    setError("");
    try {
      const { data } = await KycService.myStatus();
      setStatus(data);
    } catch (err) {
      // 404 = no submission yet — treat as null
      if (err?.response?.status === 404) {
        setStatus(null);
      } else {
        setError(extractErrorMessage(err, "Could not load KYC status."));
        setStatus(null);
      }
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const onSubmit = async (values) => {
    if (!file) {
      setError("Please attach your document.");
      return;
    }
    setSubmitting(true);
    setError("");
    try {
      const fd = new FormData();
      fd.append("contactNumber", values.contactNumber);
      fd.append("address", values.address);
      fd.append("documentType", values.documentType);
      fd.append("file", file);
      await KycService.upload(fd);
      dispatch(pushToast({ message: "KYC submitted for review.", variant: "success" }));
      reset();
      setFile(null);
      load();
    } catch (err) {
      setError(extractErrorMessage(err, "Upload failed."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6 max-w-2xl">
      <header>
        <p className="text-xs uppercase tracking-widest text-text-subtle">Verification</p>
        <h1 className="font-heading text-4xl font-semibold mt-1">KYC</h1>
        <p className="mt-2 text-text-muted">
          Verify your identity to unlock claims and policy purchases.
        </p>
      </header>

      <div className="rounded-2xl border border-border bg-surface p-5">
        <div className="flex items-center gap-3">
          <div className="grid place-items-center w-10 h-10 rounded-md bg-brand-soft text-brand">
            <ShieldCheck size={18} />
          </div>
          <div className="flex-1">
            <p className="text-sm text-text-muted">Account</p>
            <p className="font-heading text-lg font-semibold">{user?.email}</p>
          </div>
          {status === undefined ? (
            <Skeleton className="h-6 w-20" />
          ) : status?.status ? (
            <StatusBadge status={status.status} />
          ) : (
            <StatusBadge status="PENDING">NOT SUBMITTED</StatusBadge>
          )}
        </div>
      </div>

      {error && <ErrorAlert message={error} />}

      {status?.status !== "APPROVED" && (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5" noValidate>
          <Field
            id="contactNumber"
            label="Contact number"
            error={errors.contactNumber?.message}
            {...register("contactNumber")}
          />
          <Field
            id="address"
            label="Address"
            error={errors.address?.message}
            {...register("address")}
          />
          <Field
            as="select"
            id="documentType"
            label="Document type"
            error={errors.documentType?.message}
            {...register("documentType")}
          >
            <option value="">Select…</option>
            <option value="ID_CARD">ID Card</option>
            <option value="PASSPORT">Passport</option>
            <option value="DRIVERS_LICENSE">Driver's License</option>
          </Field>

          <div>
            <label htmlFor="kyc-file" className="block text-sm font-semibold text-text mb-1.5">
              Document file
            </label>
            <label
              htmlFor="kyc-file"
              className="flex flex-col items-center gap-2 px-4 py-8 rounded-xl border border-dashed border-border bg-surface text-center cursor-pointer hover:border-brand hover:bg-surface-2"
            >
              <Upload size={20} className="text-text-muted" />
              <p className="text-sm text-text">
                {file ? file.name : "Click to upload your document"}
              </p>
              <p className="text-xs text-text-subtle">PNG, JPG, PDF up to 10MB</p>
            </label>
            <input
              id="kyc-file"
              type="file"
              accept="image/*,application/pdf"
              onChange={(e) => setFile(e.target.files?.[0] || null)}
              className="sr-only"
            />
          </div>

          <Button type="submit" loading={submitting}>
            {status?.status ? "Re-submit" : "Submit for verification"}
          </Button>
        </form>
      )}
    </div>
  );
}
