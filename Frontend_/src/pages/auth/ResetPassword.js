import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useDispatch } from "react-redux";
import * as yup from "yup";
import { yupResolver } from "@hookform/resolvers/yup";
import AuthShell from "./AuthShell";
import Field from "../../components/ui/Field";
import Button from "../../components/ui/Button";
import ErrorAlert from "../../components/ui/ErrorAlert";
import { AuthService } from "../../lib/services";
import { pushToast } from "../../store/slices/toastSlice";
import { extractErrorMessage } from "../../lib/api";

const schema = yup.object({
  email: yup.string().trim().email("Invalid email").required("Email is required"),
  otp: yup.string().trim().required("OTP is required"),
  newPassword: yup
    .string()
    .min(8, "Minimum 8 characters")
    .matches(/[a-z]/, "Must include lowercase")
    .matches(/[A-Z]/, "Must include uppercase")
    .matches(/\d/, "Must include a digit")
    .matches(/[^A-Za-z0-9]/, "Must include a special character")
    .required("New password is required"),
});

export default function ResetPassword() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(schema),
    defaultValues: { email: location.state?.email || "", otp: "", newPassword: "" },
  });

  const onSubmit = async (values) => {
    setSubmitting(true);
    setError("");
    try {
      await AuthService.resetPassword(values);
      dispatch(pushToast({ message: "Password updated. Please sign in.", variant: "success" }));
      navigate("/login", { replace: true });
    } catch (err) {
      setError(extractErrorMessage(err, "Could not reset password."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AuthShell
      title="Reset password"
      subtitle="Enter the code we sent and choose a new password."
      footer={
        <p>
          <Link to="/login" className="text-brand font-semibold hover:underline">
            Back to sign in
          </Link>
        </p>
      }
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        {error && <ErrorAlert message={error} />}
        <Field id="email" label="Email" type="email" placeholder="you@example.com" error={errors.email?.message} {...register("email")} />
        <Field id="otp" label="One-time code" inputMode="numeric" placeholder="Enter OTP from email" error={errors.otp?.message} {...register("otp")} />
        <Field
          id="newPassword"
          label="New password"
          type="password"
          autoComplete="new-password"
          placeholder="Enter new password"
          error={errors.newPassword?.message}
          {...register("newPassword")}
        />
        <Button type="submit" loading={submitting} className="w-full">
          Update password
        </Button>
      </form>
    </AuthShell>
  );
}
