import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { Link, useNavigate } from "react-router-dom";
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
});

export default function ForgotPassword() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ resolver: yupResolver(schema), defaultValues: { email: "" } });

  const onSubmit = async (values) => {
    setSubmitting(true);
    setError("");
    try {
      await AuthService.forgotPassword(values);
      dispatch(pushToast({ message: "If the email exists, an OTP has been sent.", variant: "info" }));
      navigate("/reset-password", { state: { email: values.email } });
    } catch (err) {
      setError(extractErrorMessage(err, "Could not send reset email."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AuthShell
      title="Forgot password"
      subtitle="We'll send a one-time code to your email."
      footer={
        <p>
          Remembered it?{" "}
          <Link to="/login" className="text-brand font-semibold hover:underline">
            Sign in
          </Link>
        </p>
      }
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        {error && <ErrorAlert message={error} />}
        <Field id="email" label="Email" type="email" placeholder="you@example.com" error={errors.email?.message} {...register("email")} />
        <Button type="submit" loading={submitting} className="w-full">
          Send reset code
        </Button>
      </form>
    </AuthShell>
  );
}
