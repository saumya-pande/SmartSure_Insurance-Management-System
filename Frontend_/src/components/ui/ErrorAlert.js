import React from "react";
import Icon from "./Icon";

export default function ErrorAlert({ message, onRetry }) {
  if (!message) return null;

  return (
    <div
      role="alert"
      className="flex items-start gap-3 px-4 py-3 rounded-lg border border-danger/30 bg-danger-soft text-danger"
    >
      <Icon name="triangle-alert" className="mt-0.5 shrink-0" />
      <p className="flex-1 text-sm">{message}</p>
      {onRetry && (
        <button
          type="button"
          onClick={onRetry}
          className="text-sm font-semibold underline hover:no-underline"
        >
          Retry
        </button>
      )}
    </div>
  );
}
