import axios from "axios";
import { store } from "../store";
import { clearAuth } from "../store/slices/authSlice";

const baseURL = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080";

const api = axios.create({
  baseURL,
  headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use((config) => {
  const { auth } = store.getState();
  if (auth?.token) {
    config.headers.Authorization = `Bearer ${auth.token}`;
  }
  if (auth?.user?.email && !config.headers["X-User-Email"]) {
    config.headers["X-User-Email"] = auth.user.email;
  }
  if (auth?.user?.role && !config.headers["X-User-Role"]) {
    config.headers["X-User-Role"] = auth.user.role;
  }
  return config;
});

api.interceptors.response.use(
  (r) => r,
  (error) => {
    if (error?.response?.status === 401) {
      // session invalid — clear and let route guard redirect
      store.dispatch(clearAuth());
    }
    return Promise.reject(error);
  }
);

export const extractErrorMessage = (err, fallback = "Something went wrong.") => {
  if (!err) return fallback;
  const data = err?.response?.data;
  if (typeof data === "string") return data;
  return (
    data?.message ||
    data?.error ||
    err?.message ||
    fallback
  );
};

export default api;
