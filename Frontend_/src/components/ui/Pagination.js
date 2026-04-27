import React from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";

export default function Pagination({ page, totalPages, onChange }) {
  const tp = Math.max(totalPages || 1, 1);
  const current = Math.min(Math.max(page || 0, 0), tp - 1);
  const canPrev = current > 0;
  const canNext = current < tp - 1;
  return (
    <div className="flex items-center justify-between gap-3 mt-4">
      <p className="text-sm text-text-muted">
        Page <span className="font-semibold text-text">{current + 1}</span> of{" "}
        <span className="font-semibold text-text">{tp}</span>
      </p>
      <div className="flex items-center gap-2">
        <button
          type="button"
          onClick={() => onChange(current - 1)}
          disabled={!canPrev}
          className="inline-flex items-center gap-1 px-3 py-2 text-sm rounded-md border border-border bg-surface hover:bg-surface-2 disabled:opacity-40"
          aria-label="Previous page"
        >
          <ChevronLeft size={16} />
          Prev
        </button>
        <button
          type="button"
          onClick={() => onChange(current + 1)}
          disabled={!canNext}
          className="inline-flex items-center gap-1 px-3 py-2 text-sm rounded-md border border-border bg-surface hover:bg-surface-2 disabled:opacity-40"
          aria-label="Next page"
        >
          Next
          <ChevronRight size={16} />
        </button>
      </div>
    </div>
  );
}
