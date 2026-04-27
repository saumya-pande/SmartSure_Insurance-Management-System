import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { CheckCircle2, AlertTriangle, XCircle, Info, X } from "lucide-react";
import { dismissToast, selectToasts } from "../../store/slices/toastSlice";

const ICONS = {
  success: CheckCircle2,
  warning: AlertTriangle,
  danger: XCircle,
  info: Info,
};

const STYLES = {
  success: "bg-success-soft text-success border-success/30",
  warning: "bg-warning-soft text-warning border-warning/30",
  danger: "bg-danger-soft text-danger border-danger/30",
  info: "bg-info-soft text-info border-info/30",
};

export default function ToastHost() {
  const toasts = useSelector(selectToasts);
  const dispatch = useDispatch();

  useEffect(() => {
    const timers = toasts.map((t) =>
      setTimeout(() => dispatch(dismissToast(t.id)), t.duration || 4000)
    );
    return () => timers.forEach(clearTimeout);
  }, [toasts, dispatch]);

  return (
    <div
      className="fixed top-4 right-4 z-[100] flex flex-col gap-2 w-[min(360px,calc(100vw-2rem))]"
      role="status"
      aria-live="polite"
    >
      {toasts.map((t) => {
        const Icon = ICONS[t.variant] || Info;
        return (
          <div
            key={t.id}
            className={`flex items-start gap-3 px-4 py-3 rounded-lg border shadow-elevated bg-surface ${STYLES[t.variant] || STYLES.info}`}
          >
            <Icon size={18} className="mt-0.5 shrink-0" />
            <p className="flex-1 text-sm text-text">{t.message}</p>
            <button
              type="button"
              onClick={() => dispatch(dismissToast(t.id))}
              aria-label="Dismiss notification"
              className="text-text-muted hover:text-text"
            >
              <X size={16} />
            </button>
          </div>
        );
      })}
    </div>
  );
}
