import api from "./api";

export const AuthService = {
  register: (payload) => api.post("/api/auth/register", payload),
  login: (payload) => api.post("/api/auth/login", payload),
  logout: () => api.post("/api/auth/logout"),
  refresh: (refreshToken) =>
    api.post("/api/auth/refresh", null, { headers: { "Refresh-Token": refreshToken } }),
  validate: (token) => api.get("/api/auth/validate", { params: { token } }),
  forgotPassword: (payload) => api.post("/api/auth/forgot-password", payload),
  resetPassword: (payload) => api.post("/api/auth/reset-password", payload),
};

export const KycService = {
  upload: (formData) =>
    api.post("/api/kyc/upload", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    }),
  myStatus: () => api.get("/api/kyc/my"),
  myFile: () => api.get("/api/kyc/my/file", { responseType: "blob" }),
  adminFile: (id) => api.get(`/api/kyc/${id}/file`, { responseType: "blob" }),
};

export const PolicyService = {
  listActive: (params = {}) => api.get("/api/policies/active", { params }),
  getActive: (id) => api.get(`/api/policies/active/${id}`),
  purchase: (payload) => api.post("/api/policies/purchase", payload),
  myPurchases: (params = {}) => api.get("/api/policies/purchase/my", { params }),
};

export const ClaimService = {
  createDraft: (formData) =>
    api.post("/api/claims/draft", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    }),
  updateDraft: (id, formData) =>
    api.put(`/api/claims/draft/${id}`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    }),
  submit: (id) => api.patch(`/api/claims/${id}/submit`),
  myClaims: (params = {}) => api.get("/api/claims/my", { params }),
  document: (docId) => api.get(`/api/claims/document/${docId}`, { responseType: "blob" }),
};

// Admin (per rule #21 — uses /api/admin/* exactly as spec'd in API map)
export const AdminUserService = {
  list: (params = {}) => api.get("/api/admin/users", { params }),
  setStatus: (id, active) =>
    api.patch(`/api/admin/users/${id}/status`, null, { params: { active } }),
  count: () => api.get("/api/admin/users/count"),
};

export const AdminKycService = {
  list: (params = {}) => api.get("/api/admin/kyc", { params }),
  setStatus: (id, status) =>
    api.patch(`/api/admin/kyc/${id}/status`, null, { params: { status } }),
  count: () => api.get("/api/admin/kyc/count"),
};

export const AdminPolicyService = {
  list: (params = {}) => api.get("/api/admin/policies", { params }),
  listPurchased: (params = {}) => api.get("/api/admin/policies/purchased", { params }),
  count: () => api.get("/api/admin/policies/count"),
  revenue: () => api.get("/api/admin/policies/revenue"),
  create: (payload) => api.post("/api/admin/policies", payload),
  update: (id, payload) => api.put(`/api/admin/policies/${id}`, payload),
  remove: (id) => api.delete(`/api/admin/policies/${id}`),
  setStatus: (id, status) =>
    api.patch(`/api/admin/policies/${id}/status`, null, { params: { status } }),
};

export const AdminClaimService = {
  list: (params = {}) => api.get("/api/claims", { params }),
  override: (id, status) =>
    api.patch(`/api/claims/${id}/status`, null, { params: { status } }),
  count: () => api.get("/api/claims/count"),
  payouts: () => api.get("/api/claims/payouts"),
};

export const AdminDashboardService = {
  summary: () => api.get("/api/dashboard"),
};
