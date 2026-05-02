import React, { useEffect, useState, useCallback } from "react";
import { useDispatch } from "react-redux";
import { AdminUserService } from "../../lib/services";
import { extractErrorMessage } from "../../lib/api";
import ErrorAlert from "../../components/ui/ErrorAlert";
import StatusBadge from "../../components/ui/StatusBadge";
import { ListCardSkeleton } from "../../components/ui/Skeleton";
import EmptyState from "../../components/ui/EmptyState";
import Pagination from "../../components/ui/Pagination";
import Button from "../../components/ui/Button";
import Field from "../../components/ui/Field";
import { pushToast } from "../../store/slices/toastSlice";

const PAGE_SIZE = 10;

export default function AdminUsers() {
  const dispatch = useDispatch();
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [filters, setFilters] = useState({ email: "", name: "", role: "", active: "" });

  const load = useCallback(async () => {
    setData(null);
    setError("");
    try {
      const params = { page, size: PAGE_SIZE };
      if (filters.email) params.email = filters.email;
      if (filters.name) params.name = filters.name;
      if (filters.role) params.role = filters.role;
      if (filters.active) params.active = filters.active === "ACTIVE";
      const { data: response } = await AdminUserService.list(params);
      setData(response);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load users."));
      setData({ content: [], totalPages: 1, number: 0 });
    }
  }, [filters, page]);

  useEffect(() => {
    load();
  }, [load]);

  const toggle = useCallback(
    async (user) => {
      try {
        await AdminUserService.setStatus(user.id, !user.active);
        dispatch(pushToast({
          message: `User ${!user.active ? "activated" : "deactivated"}.`,
          variant: "success",
        }));
        load();
      } catch (err) {
        dispatch(pushToast({ message: extractErrorMessage(err), variant: "danger" }));
      }
    },
    [dispatch, load]
  );

  const users = data?.content || [];

  return (
    <div className="space-y-6">
      <header>
        <p className="text-xs uppercase tracking-widest text-text-subtle">Operations</p>
        <h1 className="font-heading text-4xl font-semibold mt-1">Users</h1>
      </header>

      <section className="rounded-2xl border border-border bg-surface p-4 grid md:grid-cols-4 gap-3">
        <Field id="user-email" label="Email" value={filters.email} onChange={(event) => setFilters((current) => ({ ...current, email: event.target.value }))} />
        <Field id="user-name" label="Name" value={filters.name} onChange={(event) => setFilters((current) => ({ ...current, name: event.target.value }))} />
        <Field as="select" id="user-role" label="Role" value={filters.role} onChange={(event) => setFilters((current) => ({ ...current, role: event.target.value }))}>
          <option value="">All roles</option>
          <option value="ADMIN">ADMIN</option>
          <option value="CUSTOMER">CUSTOMER</option>
        </Field>
        <Field as="select" id="user-active" label="Status" value={filters.active} onChange={(event) => setFilters((current) => ({ ...current, active: event.target.value }))}>
          <option value="">All statuses</option>
          <option value="ACTIVE">ACTIVE</option>
          <option value="INACTIVE">INACTIVE</option>
        </Field>
      </section>

      {error && <ErrorAlert message={error} onRetry={load} />}

      {data === null ? (
        <ListCardSkeleton />
      ) : users.length === 0 ? (
        <EmptyState icon="users" title="No users found" />
      ) : (
        <ul className="space-y-3">
          {users.map((user) => (
            <li key={user.id} className="rounded-xl border border-border bg-surface p-4 flex flex-wrap items-center gap-4">
              <div className="flex-1 min-w-[220px]">
                <div className="flex items-center gap-2 flex-wrap">
                  <h3 className="font-semibold">{user.name || user.email}</h3>
                  <StatusBadge status={String(user.role || "CUSTOMER").toUpperCase()} />
                  <StatusBadge status={user.active ? "ACTIVE" : "INACTIVE"} />
                </div>
                <p className="text-sm text-text-muted mt-0.5">{user.email}</p>
              </div>
              <Button variant={user.active ? "outline" : "primary"} onClick={() => toggle(user)}>
                {user.active ? "Deactivate" : "Activate"}
              </Button>
            </li>
          ))}
        </ul>
      )}

      {data && <Pagination page={data.number ?? page} totalPages={data.totalPages ?? 1} onChange={setPage} />}
    </div>
  );
}
