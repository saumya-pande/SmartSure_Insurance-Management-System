import React, { useEffect, useRef, useState, useCallback } from "react";
import Button from "./Button";
import Icon from "./Icon";
import api from "../../lib/api";

export default function FilePreviewDialog({ file, title = "Document preview", onClose }) {
  const closeRef = useRef(null);
  const onCloseRef = useRef(onClose);
  const [blobUrl, setBlobUrl] = useState(null);
  const [contentType, setContentType] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    onCloseRef.current = onClose;
  }, [onClose]);

  // Fetch file as blob with auth headers when file changes
  useEffect(() => {
    if (!file?.viewUrl) {
      setBlobUrl(null);
      setContentType("");
      return undefined;
    }

    let revoked = false;
    setLoading(true);
    setError("");

    api
      .get(file.viewUrl, { responseType: "blob" })
      .then((response) => {
        if (revoked) return;
        const blob = response.data;
        const url = URL.createObjectURL(blob);
        setBlobUrl(url);
        setContentType(blob.type || "");
      })
      .catch(() => {
        if (revoked) return;
        setError("Could not load document.");
      })
      .finally(() => {
        if (!revoked) setLoading(false);
      });

    return () => {
      revoked = true;
      setBlobUrl((prev) => {
        if (prev) URL.revokeObjectURL(prev);
        return null;
      });
      setContentType("");
    };
  }, [file?.viewUrl]);

  useEffect(() => {
    if (!file) return undefined;
    closeRef.current?.focus();
    const onKeyDown = (event) => {
      if (event.key === "Escape") onCloseRef.current();
    };
    window.addEventListener("keydown", onKeyDown);
    return () => {
      window.removeEventListener("keydown", onKeyDown);
    };
  }, [file]);

  const handleDownload = useCallback(() => {
    if (!blobUrl) return;
    const link = document.createElement("a");
    link.href = blobUrl;
    link.download = file?.fileName || "document";
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }, [blobUrl, file?.fileName]);

  if (!file) return null;

  const isImage = contentType.startsWith("image/");

  return (
    <div className="fixed inset-0 z-[100] bg-black/50 p-4 flex items-center justify-center" role="dialog" aria-modal="true" aria-labelledby="file-preview-title">
      <div className="w-full max-w-5xl max-h-[90vh] rounded-xl border border-border bg-surface shadow-elevated overflow-hidden flex flex-col">
        <div className="flex items-center gap-3 border-b border-border px-4 py-3">
          <div className="grid place-items-center w-9 h-9 rounded-md bg-brand-soft text-brand">
            <Icon name="file" />
          </div>
          <div className="min-w-0 flex-1">
            <h2 id="file-preview-title" className="font-heading text-xl font-semibold truncate">{title}</h2>
            <p className="text-sm text-text-muted truncate">{file.fileName || "Document"}</p>
          </div>
          {blobUrl && (
            <Button type="button" variant="outline" onClick={handleDownload}>
              <Icon name="download" />
              Download
            </Button>
          )}
          <Button ref={closeRef} type="button" variant="ghost" onClick={onClose} aria-label="Close preview" className="px-3">
            <Icon name="close" />
          </Button>
        </div>

        <div className="bg-surface-2 h-[70vh] min-h-[360px] flex items-center justify-center overflow-auto p-4">
          {loading ? (
            <p className="text-text-muted text-sm">Loading document…</p>
          ) : error ? (
            <p className="text-danger text-sm">{error}</p>
          ) : blobUrl ? (
            isImage ? (
              <img
                src={blobUrl}
                alt={file.fileName || "Document preview"}
                style={{ maxWidth: "100%", maxHeight: "100%", objectFit: "contain" }}
              />
            ) : (
              <iframe
                title={file.fileName || "Document preview"}
                src={blobUrl}
                className="w-full h-full bg-white"
              />
            )
          ) : (
            <p className="text-text-muted text-sm">No document to display.</p>
          )}
        </div>
      </div>
    </div>
  );
}
