import React, { useEffect, useState, useCallback } from "react";
import { useForm } from "react-hook-form";
import { useDispatch, useSelector } from "react-redux";
import Field from "../../components/ui/Field";
import Button from "../../components/ui/Button";
import ErrorAlert from "../../components/ui/ErrorAlert";
import StatusBadge from "../../components/ui/StatusBadge";
import { Skeleton } from "../../components/ui/Skeleton";
import { KycService } from "../../lib/services";
import { selectAuth } from "../../store/slices/authSlice";
import { pushToast } from "../../store/slices/toastSlice";
import { extractErrorMessage } from "../../lib/api";
import { openBlobResponse } from "../../lib/file";
import Icon from "../../components/ui/Icon";

export default function MyKyc() {
  const dispatch = useDispatch();
  const { user } = useSelector(selectAuth);
  const [status, setStatus] = useState(undefined);
  const [error, setError] = useState("");
  const [file, setFile] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [openingFile, setOpeningFile] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    defaultValues: { contactNumber: "", address: "", documentType: "" },
  });

  const load = useCallback(async () => {
    setStatus(undefined);
    setError("");
    try {
      const { data } = await KycService.myStatus();
      setStatus(data);
    } catch (err) {
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

  const onSubmit = useCallback(
    async (values) => {
      if (!file) {
        setError("Please attach your document.");
        return;
      }
      setSubmitting(true);
      setError("");
      try {
        const formData = new FormData();
        formData.append("contactNumber", values.contactNumber);
        formData.append("address", values.address);
        formData.append("documentType", values.documentType);
        formData.append("file", file);
        await KycService.upload(formData);
        dispatch(pushToast({ message: "KYC submitted for review.", variant: "success" }));
        setFile(null);
        load();
      } catch (err) {
        setError(extractErrorMessage(err, "Upload failed."));
      } finally {
        setSubmitting(false);
      }
    },
    [dispatch, file, load]
  );

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
    <div className="space-y-6 max-w-2xl">
      <header>
        <p className="text-xs uppercase tracking-widest text-text-subtle">Verification</p>
        <h1 className="font-heading text-4xl font-semibold mt-1">KYC</h1>
        <p className="mt-2 text-text-muted">Verify identity to keep purchases and claims moving.</p>
      </header>

      <div className="rounded-2xl border border-border bg-surface p-5">
        <div className="flex items-center gap-3">
          <div className="grid place-items-center w-10 h-10 rounded-md bg-brand-soft text-brand">
            <Icon name="shield" />
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
            <StatusBadge status="PENDING">Not Submitted</StatusBadge>
          )}
        </div>
        {status?.status && (
          <div className="mt-4 flex flex-wrap gap-3">
            <Button type="button" variant="outline" onClick={openFile} loading={openingFile}>
              View uploaded file
            </Button>
          </div>
        )}
      </div>

      {error && <ErrorAlert message={error} />}

      {status?.status !== "APPROVED" && (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5" noValidate>
          <Field
            id="contactNumber"
            label="Contact number"
            error={errors.contactNumber?.message}
            {...register("contactNumber", { required: "Contact number is required" })}
          />
          <Field
            id="address"
            label="Address"
            error={errors.address?.message}
            {...register("address", { required: "Address is required" })}
          />
          <Field
            as="select"
            id="documentType"
            label="Document type"
            error={errors.documentType?.message}
            {...register("documentType", { required: "Select a document type" })}
          >
            <option value="">Select</option>
            <option value="ID_CARD">ID Card</option>
            <option value="PASSPORT">Passport</option>
            <option value="DRIVERS_LICENSE">Driver&apos;s License</option>
          </Field>

          <div>
            <label htmlFor="kyc-file" className="block text-sm font-semibold text-text mb-1.5">
              Document file
            </label>
            <label
              htmlFor="kyc-file"
              className="flex flex-col items-center gap-2 px-4 py-8 rounded-xl border border-dashed border-border bg-surface text-center cursor-pointer hover:border-brand hover:bg-surface-2"
            >
              <span className="grid place-items-center w-12 h-12 rounded-full bg-brand-soft text-brand">
                <Icon name="files" />
              </span>
              <p className="text-sm text-text">{file ? file.name : "Click to upload your document"}</p>
              <p className="text-xs text-text-subtle">PNG, JPG, PDF up to 10MB</p>
            </label>
            <input
              id="kyc-file"
              type="file"
              accept="image/*,application/pdf"
              onChange={(event) => setFile(event.target.files?.[0] || null)}
              className="sr-only"
            />
          </div>

          <Button type="submit" loading={submitting}>
            {status?.status ? "Re-submit KYC" : "Submit KYC"}
          </Button>
        </form>
      )}
    </div>
  );
}
