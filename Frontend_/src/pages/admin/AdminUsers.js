import React, { useEffect, useState, useCallback, memo } from "react";
import { useDispatch } from "react-redux";
import { User } from "lucide-react";
import { AdminUserService } from "../../lib/services";
import { extractErrorMessage } from "../../lib/api";
import ErrorAlert from "../../components/ui/ErrorAlert";
import StatusBadge from "../../components/ui/StatusBadge";
import { ListCardSkeleton } from "../../components/ui/Skeleton";
import EmptyState from "../../components/ui/EmptyState";
import Pagination from "../../components/ui/Pagination";
import Button from "../../components/ui/Button";
import { pushToast } from "../../store/slices/toastSlice";

const PAGE_SIZE = 10;

export default function AdminUsers() {
  const dispatch = useDispatch();
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    setData(null);
    setError("");
    try {
      const { data: res } = await AdminUserService.list({ page, size: PAGE_SIZE });
      setData(res);
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load users."));
      setData({ content: [], totalElements: 0, totalPages: 1, number: 0 });
    }
  }, [page]);

  useEffect(() => {
    load();
  }, [load]);

  const toggle = useCallback(
    async (u) => {
      try {
        await AdminUserService.setStatus(u.id, !u.active);
        dispatch(pushToast({
          message: `User ${!u.active ? "activated" : "deactivated"}.`,
          variant: "success",
        }));
        load();
      } catch (err) {
        dispatch(pushToast({ message: extractErrorMessage(err), variant: "danger" }));
      }
    },
    [dispatch, load]
  );

  const list = data?.content || [];

  return (
    <div className="space-y-6">
      <header>
        <p className="text-xs uppercase tracking-widest text-text-subtle">Operations</p>
        <h1 className="font-heading text-4xl font-semibold mt-1">Users</h1>
      </header>

      {error && <ErrorAlert message={error} onRetry={load} />}

      {data === null ? (
        <ListCardSkeleton />
      ) : list.length === 0 ? (
        <EmptyState icon={User} title="No users found" />
      ) : (
        <ul className="space-y-3">
          {list.map((u) => (
            <UserRow key={u.id} user={u} onToggle={toggle} />
          ))}
        </ul>
      )}

      {data && (
        <Pagination
          page={data.number ?? page}
          totalPages={data.totalPages ?? 1}
          onChange={setPage}
        />
      )}
    </div>
  );
}

const UserRow = memo(function UserRow({ user, onToggle }) {
  const handle = useCallback(() => onToggle(user), [onToggle, user]);
  return (
    <li className="rounded-xl border border-border bg-surface p-4 flex flex-wrap items-center gap-4">
      <div className="grid place-items-center w-10 h-10 rounded-full bg-brand-soft text-brand">
        <User size={18} />
      </div>
      <div className="flex-1 min-w-[200px]">
        <div className="flex items-center gap-2 flex-wrap">
          <h3 className="font-semibold">{user.name || user.email}</h3>
          <StatusBadge status={String(user.role || "CUSTOMER").toUpperCase()} />
          <StatusBadge status={user.active ? "ACTIVE" : "INACTIVE"} />
        </div>
        <p className="text-sm text-text-muted mt-0.5">{user.email}</p>
      </div>
      <Button variant={user.active ? "outline" : "primary"} onClick={handle}>
        {user.active ? "Deactivate" : "Activate"}
      </Button>
    </li>
  );
});
