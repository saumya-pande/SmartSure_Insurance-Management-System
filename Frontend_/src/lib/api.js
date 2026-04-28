import axios from "axios";
import { store } from "../store";
import { clearAuth, setCredentials } from "../store/slices/authSlice";

const envBaseURL = process.env.REACT_APP_API_BASE_URL || "";
const useDevProxy =
  process.env.NODE_ENV === "development" &&
  (!envBaseURL || /localhost:8080/i.test(envBaseURL) || /127\.0\.0\.1:8080/i.test(envBaseURL));

const baseURL = useDevProxy ? "" : envBaseURL || "http://localhost:8080";

const api = axios.create({
  baseURL,
  headers: { "Content-Type": "application/json" },
});

const isPublicAuthRequest = (url) => {
  const value = String(url || "");
  return [
    "/api/auth/login",
    "/api/auth/register",
    "/api/auth/forgot-password",
    "/api/auth/reset-password",
    "/api/auth/validate",
    "/api/auth/refresh",
  ].some((path) => value.includes(path));
};

api.interceptors.request.use((config) => {
  const { auth } = store.getState();
  const skipAuthHeaders = isPublicAuthRequest(config.url);

  if (auth?.token && !skipAuthHeaders) {
    config.headers.Authorization = `Bearer ${auth.token}`;
  }
  if (auth?.user?.email && !skipAuthHeaders && !config.headers["X-User-Email"]) {
    config.headers["X-User-Email"] = auth.user.email;
  }
  if (auth?.user?.role && !skipAuthHeaders && !config.headers["X-User-Role"]) {
    const role = String(auth.user.role).startsWith("ROLE_")
      ? String(auth.user.role)
      : `ROLE_${auth.user.role}`;
    config.headers["X-User-Role"] = role;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error?.config;
    const refreshToken = store.getState()?.auth?.refreshToken;

    if (
      error?.response?.status === 401 &&
      refreshToken &&
      !originalRequest?._retry &&
      !String(originalRequest?.url || "").includes("/api/auth/refresh")
    ) {
      originalRequest._retry = true;
      try {
        const refreshResponse = await axios.post(
          `${baseURL}/api/auth/refresh`,
          null,
          { headers: { "Refresh-Token": refreshToken } }
        );
        const authState = store.getState().auth;
        const nextToken = refreshResponse?.data?.token || refreshResponse?.data?.accessToken;
        if (!nextToken) {
          throw new Error("Missing refreshed token");
        }
        store.dispatch(
          setCredentials({
            token: nextToken,
            refreshToken: refreshResponse?.data?.refreshToken || refreshToken,
            user: {
              ...(authState.user || {}),
              role: refreshResponse?.data?.role || authState.user?.role,
            },
          })
        );
        originalRequest.headers = {
          ...(originalRequest.headers || {}),
          Authorization: `Bearer ${nextToken}`,
        };
        return api(originalRequest);
      } catch (refreshError) {
        store.dispatch(clearAuth());
        return Promise.reject(refreshError);
      }
    }

    if (error?.response?.status === 401) {
      store.dispatch(clearAuth());
    }
    return Promise.reject(error);
  }
);

export const extractErrorMessage = (err, fallback = "Something went wrong.") => {
  if (!err) return fallback;
  const data = err?.response?.data;
  if (typeof data === "string") return data;
  if (Array.isArray(data?.errors) && data.errors.length > 0) {
    return data.errors.map((item) => item?.message).filter(Boolean).join(", ");
  }
  return data?.message || data?.error || err?.message || fallback;
};

export default api;
