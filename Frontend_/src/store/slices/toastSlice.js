import { createSlice } from "@reduxjs/toolkit";

let nextId = 1;

const slice = createSlice({
  name: "toast",
  initialState: { items: [] },
  reducers: {
    pushToast: {
      reducer(state, { payload }) {
        state.items.push(payload);
      },
      prepare({ message, variant = "info", duration = 4000 }) {
        return {
          payload: { id: nextId++, message, variant, duration },
        };
      },
    },
    dismissToast(state, { payload }) {
      state.items = state.items.filter((t) => t.id !== payload);
    },
  },
});

export const { pushToast, dismissToast } = slice.actions;
export const selectToasts = (s) => s.toast.items;
export default slice.reducer;
