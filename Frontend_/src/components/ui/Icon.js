import React from "react";

const ICONS = {
  "arrow-left": "fa-arrow-left",
  "arrow-right": "fa-arrow-right",
  "bolt": "fa-bolt",
  "car": "fa-car-side",
  "check-circle": "fa-circle-check",
  "claims": "fa-file-circle-check",
  "clipboard": "fa-clipboard-list",
  "close": "fa-xmark",
  "download": "fa-download",
  "edit": "fa-pen-to-square",
  "eye": "fa-eye",
  "eye-slash": "fa-eye-slash",
  "file": "fa-file-lines",
  "files": "fa-copy",
  "folder-open": "fa-folder-open",
  "home": "fa-house",
  "id-card": "fa-id-card",
  "image": "fa-image",
  "info": "fa-circle-info",
  "lock": "fa-lock",
  "logout": "fa-right-from-bracket",
  "menu": "fa-bars",
  "moon": "fa-moon",
  "plus": "fa-plus",
  "search": "fa-magnifying-glass",
  "shield": "fa-shield-halved",
  "spinner": "fa-spinner",
  "sun": "fa-sun",
  "trash": "fa-trash",
  "triangle-alert": "fa-triangle-exclamation",
  "upload": "fa-cloud-arrow-up",
  "user": "fa-user",
  "users": "fa-users",
  "vehicle": "fa-car-side",
  "wallet": "fa-wallet",
  "revenue": "fa-chart-line",
};

const SIZE_CLASSES = {
  xs: "text-xs",
  sm: "text-sm",
  md: "text-base",
  lg: "text-lg",
  xl: "text-xl",
  "2xl": "text-2xl",
};

export default function Icon({
  name,
  className = "",
  size = "md",
  spin = false,
  fixedWidth = false,
}) {
  const iconClass = ICONS[name] || "fa-circle-question";
  const sizeClass = SIZE_CLASSES[size] || "";
  return (
    <i
      aria-hidden="true"
      className={`fa-solid ${iconClass} ${sizeClass} ${spin ? "animate-spin" : ""} ${fixedWidth ? "fa-fw" : ""} ${className}`.trim()}
    />
  );
}
