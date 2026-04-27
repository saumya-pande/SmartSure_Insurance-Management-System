import React from "react";

export default function PageLoader() {
  return (
    <div className="min-h-screen p-8 bg-bg">
      <div className="max-w-5xl mx-auto space-y-4">
        <div className="skeleton h-10 w-48" />
        <div className="skeleton h-4 w-72" />
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mt-8">
          <div className="skeleton h-28" />
          <div className="skeleton h-28" />
          <div className="skeleton h-28" />
        </div>
        <div className="skeleton h-72 mt-4" />
      </div>
    </div>
  );
}
