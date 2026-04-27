import { createSlice } from "@reduxjs/toolkit";

const STORAGE_KEY = "smartsure_auth_v1";

const loadInitial = () => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return { token: null, refreshToken: null, user: null };
    const parsed = JSON.parse(raw);
    return {
      token: parsed.token || null,
      refreshToken: parsed.refreshToken || null,
      user: parsed.user || null,
    };
  } catch {
    return { token: null, refreshToken: null, user: null };
  }
};

const persist = (state) => {
  try {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        token: state.token,
        refreshToken: state.refreshToken,
        user: state.user,
      })
    );
  } catch {
    /* noop */
  }
};

const slice = createSlice({
  name: "auth",
  initialState: loadInitial(),
  reducers: {
    setCredentials(state, { payload }) {
      state.token = payload.token || null;
      state.refreshToken = payload.refreshToken || null;
      state.user = payload.user || null;
      persist(state);
    },
    updateUser(state, { payload }) {
      state.user = { ...(state.user || {}), ...payload };
      persist(state);
    },
    clearAuth(state) {
      state.token = null;
      state.refreshToken = null;
      state.user = null;
      try {
        localStorage.removeItem(STORAGE_KEY);
      } catch {
        /* noop */
      }
    },
  },
});

export const { setCredentials, updateUser, clearAuth } = slice.actions;

export const selectAuth = (s) => s.auth;
export const selectIsAuthenticated = (s) => Boolean(s.auth.token);
export const selectRole = (s) => s.auth.user?.role || null;
export const selectEmail = (s) => s.auth.user?.email || null;

export default slice.reducer;
