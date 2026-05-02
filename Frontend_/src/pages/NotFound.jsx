import React from "react";
import { Link } from "react-router-dom";

export default function NotFound() {
  return (
    <div className="min-h-screen grid place-items-center px-4 bg-bg">
      <div className="text-center max-w-md">
        <p className="text-xs uppercase tracking-widest text-brand font-semibold">Error 404</p>
        <h1 className="font-heading text-6xl font-semibold mt-2">Page not found</h1>
        <p className="mt-3 text-text-muted">
          The page you're looking for doesn't exist or has been moved.
        </p>
        <Link
          to="/"
          className="inline-flex mt-6 px-5 py-3 rounded-md bg-brand text-white font-semibold hover:bg-brand-hover"
        >
          Back to home
        </Link>
      </div>
    </div>
  );
}
