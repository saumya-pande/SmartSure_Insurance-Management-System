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
import { setCredentials } from "../../store/slices/authSlice";
import { pushToast } from "../../store/slices/toastSlice";
import { extractErrorMessage } from "../../lib/api";

const schema = yup.object({
  email: yup.string().trim().email("Invalid email").required("Email is required"),
  password: yup.string().min(8, "Minimum 8 characters").required("Password is required"),
});

export default function Login() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ resolver: yupResolver(schema), defaultValues: { email: "", password: "" } });

  const onSubmit = async (values) => {
    setSubmitting(true);
    setError("");
    try {
      const { data } = await AuthService.login(values);
      const token = data?.token || data?.accessToken;
      const refreshToken = data?.refreshToken || null;
      const user =
        data?.user || {
          email: data?.email || values.email,
          name: data?.name,
          role: data?.role || "CUSTOMER",
        };
      dispatch(setCredentials({ token, refreshToken, user }));
      dispatch(pushToast({ message: `Welcome back, ${user.name || user.email}.`, variant: "success" }));
      const from = location.state?.from;
      const target = from || (user.role === "ADMIN" ? "/admin" : "/app");
      navigate(target, { replace: true });
    } catch (err) {
      setError(extractErrorMessage(err, "Unable to sign in. Check your credentials."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AuthShell
      title="Welcome back"
      subtitle="Sign in to manage your policies and claims."
      footer={
        <p>
          Don't have an account?{" "}
          <Link to="/register" className="text-brand font-semibold hover:underline">
            Create one
          </Link>
        </p>
      }
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        {error && <ErrorAlert message={error} />}
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
          autoComplete="current-password"
          placeholder="Enter your password"
          error={errors.password?.message}
          {...register("password")}
        />
        <div className="flex items-center justify-between">
          <Link to="/forgot-password" className="text-sm text-brand font-semibold hover:underline">
            Forgot password?
          </Link>
        </div>
        <Button type="submit" loading={submitting} className="w-full">
          Sign in
        </Button>
      </form>
    </AuthShell>
  );
}
