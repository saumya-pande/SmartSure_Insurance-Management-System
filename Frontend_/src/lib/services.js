import api from "./api";

export const AuthService = {
  register: (payload) => api.post("/api/auth/register", payload),
  login: (payload) => api.post("/api/auth/login", payload),
  logout: () => api.post("/api/auth/logout"),
  forgotPassword: (payload) => api.post("/api/auth/forgot-password", payload),
  resetPassword: (payload) => api.post("/api/auth/reset-password", payload),
};

export const KycService = {
  upload: (formData) =>
    api.post("/api/kyc/upload", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    }),
  myStatus: () => api.get("/api/kyc/my"),
};

export const PolicyService = {
  listActive: (params = {}) => api.get("/api/policies/active", { params }),
  getActive: (id) => api.get(`/api/policies/active/${id}`),
  purchase: (payload) => api.post("/api/policies/purchase", payload),
  myPurchases: () => api.get("/api/policies/purchase/my"),
};

export const ClaimService = {
  fileClaim: (formData) =>
    api.post("/api/claims", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    }),
  myClaims: () => api.get("/api/claims/my"),
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
  list: (params = {}) => api.get("/api/admin/claims", { params }),
  override: (id, status) =>
    api.patch(`/api/admin/claims/${id}/status`, null, { params: { status } }),
};
