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
  name: yup
    .string()
    .trim()
    .matches(/^[A-Za-z ]{2,100}$/, "2–100 letters only")
    .required("Name is required"),
  email: yup.string().trim().email("Invalid email").required("Email is required"),
  password: yup
    .string()
    .min(8, "Minimum 8 characters")
    .matches(/[a-z]/, "Must include lowercase")
    .matches(/[A-Z]/, "Must include uppercase")
    .matches(/\d/, "Must include a digit")
    .matches(/[^A-Za-z0-9]/, "Must include a special character")
    .required("Password is required"),
  confirmPassword: yup
    .string()
    .oneOf([yup.ref("password"), null], "Passwords must match")
    .required("Please confirm your password"),
});

export default function Register() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(schema),
    defaultValues: { name: "", email: "", password: "", confirmPassword: "" },
  });

  const onSubmit = async (values) => {
    setSubmitting(true);
    setError("");
    try {
      const { confirmPassword, ...payload } = values;
      await AuthService.register(payload);
      dispatch(pushToast({ message: "Account created. Please sign in.", variant: "success" }));
      reset();
      navigate("/login", { replace: true });
    } catch (err) {
      setError(extractErrorMessage(err, "Registration failed. Please try again."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AuthShell
      title="Create your account"
      subtitle="Join SmartSure in under a minute."
      footer={
        <p>
          Already have an account?{" "}
          <Link to="/login" className="text-brand font-semibold hover:underline">
            Sign in
          </Link>
        </p>
      }
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        {error && <ErrorAlert message={error} />}
        <Field
          id="name"
          label="Full name"
          autoComplete="name"
          placeholder="John Doe"
          error={errors.name?.message}
          {...register("name")}
        />
        <Field
          id="email"
          label="Email"
          type="email"
          autoComplete="email"
          placeholder="you@example.com"
          error={errors.email?.message}
          {...register("email")}
        />
        <Field
          id="password"
          label="Password"
          type="password"
          autoComplete="new-password"
          placeholder="Create a strong password"
          hint="Min 8 chars · 1 upper · 1 lower · 1 digit · 1 special"
          error={errors.password?.message}
          {...register("password")}
        />
        <Field
          id="confirmPassword"
          label="Confirm password"
          type="password"
          autoComplete="new-password"
          placeholder="Re-enter your password"
          error={errors.confirmPassword?.message}
          {...register("confirmPassword")}
        />
        <Button type="submit" loading={submitting} className="w-full">
          Create account
        </Button>
      </form>
    </AuthShell>
  );
}
