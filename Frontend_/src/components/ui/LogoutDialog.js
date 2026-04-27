import React, { useEffect, useState, useCallback } from "react";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";
import { Loader2, LogOut, X } from "lucide-react";
import { AuthService } from "../../lib/services";
import { clearAuth } from "../../store/slices/authSlice";
import { pushToast } from "../../store/slices/toastSlice";

export default function LogoutDialog({ open, onClose }) {
  const [loading, setLoading] = useState(false);
  const dispatch = useDispatch();
  const navigate = useNavigate();

  useEffect(() => {
    if (!open) return undefined;
    const onKey = (e) => {
      if (e.key === "Escape" && !loading) onClose();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open, onClose, loading]);

  const confirm = useCallback(async () => {
    setLoading(true);
    try {
      await AuthService.logout();
    } catch {
      // ignore — we still log out client-side
    } finally {
      dispatch(clearAuth());
      dispatch(pushToast({ message: "You have been signed out.", variant: "info" }));
      setLoading(false);
      onClose();
      navigate("/login", { replace: true });
    }
  }, [dispatch, navigate, onClose]);

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-[90] grid place-items-center px-4">
      <div className="absolute inset-0 bg-black/50" onClick={loading ? undefined : onClose} aria-hidden="true" />
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="logout-title"
        className="relative w-full max-w-md rounded-2xl bg-surface border border-border shadow-elevated p-6"
      >
        <button
          type="button"
          aria-label="Close dialog"
          onClick={onClose}
          disabled={loading}
          className="absolute top-3 right-3 p-1.5 rounded-md text-text-muted hover:bg-surface-2"
        >
          <X size={16} />
        </button>
        <div className="flex items-start gap-4">
          <div className="grid place-items-center w-10 h-10 rounded-full bg-danger-soft text-danger shrink-0">
            <LogOut size={18} />
          </div>
          <div className="flex-1">
            <h2 id="logout-title" className="font-heading text-xl font-semibold">
              Sign out of SmartSure?
            </h2>
            <p className="mt-1 text-sm text-text-muted">
              You'll need to sign in again to access your dashboard, policies, and claims.
            </p>
          </div>
        </div>
        <div className="mt-6 flex justify-end gap-2">
          <button
            type="button"
            onClick={onClose}
            disabled={loading}
            className="px-4 py-2 text-sm font-semibold rounded-md border border-border hover:bg-surface-2 disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={confirm}
            disabled={loading}
            className="inline-flex items-center gap-2 px-4 py-2 text-sm font-semibold rounded-md bg-danger text-white hover:opacity-95 disabled:opacity-60"
          >
            {loading && <Loader2 size={16} className="animate-spin" />}
            Sign out
          </button>
        </div>
      </div>
    </div>
  );
}
