import React from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useSelector } from "react-redux";
import { selectAuth } from "../store/slices/authSlice";

export default function ProtectedRoute({ children, roles }) {
  const { token, user } = useSelector(selectAuth);
  const location = useLocation();

  if (!token) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  }
  if (roles && roles.length > 0 && !roles.includes(user?.role)) {
    // Send the user to their own area
    const fallback = user?.role === "ADMIN" ? "/admin" : "/app";
    return <Navigate to={fallback} replace />;
  }
  return children;
}
