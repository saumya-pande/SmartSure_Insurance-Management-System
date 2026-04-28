export const ROLES = { ADMIN: "ADMIN", CUSTOMER: "CUSTOMER" };

export const POLICY_TYPES = ["HOME", "VEHICLE"];
export const PURCHASE_STATUSES = ["ACTIVE", "EXPIRED", "CANCELLED"];
export const USER_STATUS_OPTIONS = ["ACTIVE", "INACTIVE"];

export const POLICY_STATUSES = [
  "ACTIVE",
  "INACTIVE",
  "EXPIRED",
  "CANCELLED",
  "DRAFT",
];

export const CLAIM_STATUSES = [
  "DRAFT",
  "SUBMITTED",
  "UNDER_REVIEW",
  "APPROVED",
  "REJECTED",
  "CLOSED",
];

export const KYC_STATUSES = ["PENDING", "APPROVED", "REJECTED"];

export const statusVariant = (status) => {
  const s = String(status || "").toUpperCase();
  if (["ACTIVE", "APPROVED"].includes(s)) return "success";
  if (["PENDING", "SUBMITTED", "UNDER_REVIEW", "DRAFT"].includes(s)) return "warning";
  if (["REJECTED", "EXPIRED", "CANCELLED", "INACTIVE"].includes(s)) return "danger";
  if (["CLOSED"].includes(s)) return "info";
  return "neutral";
};

export const formatCurrency = (n) => {
  const v = Number(n || 0);
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  }).format(v);
};

export const formatStatusLabel = (value, fallback = "Unknown") => {
  const raw = String(value || "").trim();
  if (!raw) return fallback;
  return raw
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
};
