import api from "./api";

export async function openApiFile(url) {
  const response = await api.get(url, { responseType: "blob" });
  return openBlobResponse(response);
}

export function openBlobResponse(response) {
  const blobUrl = window.URL.createObjectURL(response.data);
  window.open(blobUrl, "_blank", "noopener,noreferrer");
  setTimeout(() => window.URL.revokeObjectURL(blobUrl), 60_000);
}
