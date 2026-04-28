import React, { Suspense, lazy } from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import ToastHost from "./components/ui/ToastHost";
import PageLoader from "./components/ui/PageLoader";
import ProtectedRoute from "./routes/ProtectedRoute";
import PublicLayout from "./layouts/PublicLayout";
import CustomerLayout from "./layouts/CustomerLayout";
import AdminLayout from "./layouts/AdminLayout";

const Landing = lazy(() => import("./pages/Landing"));
const Login = lazy(() => import("./pages/auth/Login"));
const Register = lazy(() => import("./pages/auth/Register"));
const ForgotPassword = lazy(() => import("./pages/auth/ForgotPassword"));
const ResetPassword = lazy(() => import("./pages/auth/ResetPassword"));
const NotFound = lazy(() => import("./pages/NotFound"));

const CustomerDashboard = lazy(() => import("./pages/customer/CustomerDashboard"));
const BrowsePolicies = lazy(() => import("./pages/customer/BrowsePolicies"));
const PolicyDetail = lazy(() => import("./pages/customer/PolicyDetail"));
const MyPolicies = lazy(() => import("./pages/customer/MyPolicies"));
const MyClaims = lazy(() => import("./pages/customer/MyClaims"));
const FileClaim = lazy(() => import("./pages/customer/FileClaim"));
const MyKyc = lazy(() => import("./pages/customer/MyKyc"));
const CustomerProfile = lazy(() => import("./pages/customer/CustomerProfile"));

const AdminDashboard = lazy(() => import("./pages/admin/AdminDashboard"));
const AdminUsers = lazy(() => import("./pages/admin/AdminUsers"));
const AdminKyc = lazy(() => import("./pages/admin/AdminKyc"));
const AdminPolicies = lazy(() => import("./pages/admin/AdminPolicies"));
const AdminClaims = lazy(() => import("./pages/admin/AdminClaims"));
const AdminPurchases = lazy(() => import("./pages/admin/AdminPurchases"));

export default function App() {
  return (
    <>
      <Suspense fallback={<PageLoader />}>
        <Routes>
          <Route element={<PublicLayout />}>
            <Route path="/" element={<Landing />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/reset-password" element={<ResetPassword />} />
          </Route>

          <Route
            element={
              <ProtectedRoute roles={["CUSTOMER"]}>
                <CustomerLayout />
              </ProtectedRoute>
            }
          >
            <Route path="/app" element={<CustomerDashboard />} />
            <Route path="/app/policies" element={<BrowsePolicies />} />
            <Route path="/app/policies/:id" element={<PolicyDetail />} />
            <Route path="/app/my-policies" element={<MyPolicies />} />
            <Route path="/app/claims" element={<MyClaims />} />
            <Route path="/app/claims/new" element={<FileClaim />} />
            <Route path="/app/kyc" element={<MyKyc />} />
            <Route path="/app/profile" element={<CustomerProfile />} />
          </Route>

          <Route
            element={
              <ProtectedRoute roles={["ADMIN"]}>
                <AdminLayout />
              </ProtectedRoute>
            }
          >
            <Route path="/admin" element={<AdminDashboard />} />
            <Route path="/admin/users" element={<AdminUsers />} />
            <Route path="/admin/kyc" element={<AdminKyc />} />
            <Route path="/admin/policies" element={<AdminPolicies />} />
            <Route path="/admin/purchases" element={<AdminPurchases />} />
            <Route path="/admin/claims" element={<AdminClaims />} />
          </Route>

          <Route path="/404" element={<NotFound />} />
          <Route path="*" element={<Navigate to="/404" replace />} />
        </Routes>
      </Suspense>
      <ToastHost />
    </>
  );
}
