import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { dismissToast, selectToasts } from "../../store/slices/toastSlice";
import Icon from "./Icon";

const ICONS = {
  success: "check-circle",
  warning: "triangle-alert",
  danger: "triangle-alert",
  info: "info",
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
    const timers = toasts.map((toast) =>
      setTimeout(() => dispatch(dismissToast(toast.id)), toast.duration || 4000)
    );
    return () => timers.forEach(clearTimeout);
  }, [toasts, dispatch]);

  return (
    <div
      className="fixed top-4 right-4 z-[100] flex flex-col gap-2 w-[min(360px,calc(100vw-2rem))]"
      role="status"
      aria-live="polite"
    >
      {toasts.map((toast) => (
        <div
          key={toast.id}
          className={`flex items-start gap-3 px-4 py-3 rounded-lg border shadow-elevated bg-surface ${STYLES[toast.variant] || STYLES.info}`}
        >
          <Icon name={ICONS[toast.variant] || "info"} className="mt-0.5 shrink-0" />
          <p className="flex-1 text-sm text-text">{toast.message}</p>
          <button
            type="button"
            onClick={() => dispatch(dismissToast(toast.id))}
            aria-label="Dismiss notification"
            className="text-text-muted hover:text-text"
          >
            <Icon name="close" />
          </button>
        </div>
      ))}
    </div>
  );
}
