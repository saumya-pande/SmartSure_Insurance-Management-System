import React, { useCallback, useEffect, useMemo, useState } from "react";
import { useSelector } from "react-redux";
import { AdminDashboardService } from "../../lib/services";
import { extractErrorMessage } from "../../lib/api";
import { selectAuth } from "../../store/slices/authSlice";
import ErrorAlert from "../../components/ui/ErrorAlert";
import { Skeleton } from "../../components/ui/Skeleton";
import { formatCurrency } from "../../lib/constants";
import Icon from "../../components/ui/Icon";

const REVENUE_VIEWS = {
  GRAPH: "graph",
  PIE: "pie",
};

const CHART_COLORS = ["#1D4ED8", "#0F766E", "#D97706", "#DC2626", "#7C3AED", "#475569"];

export default function AdminDashboard() {
  const { user } = useSelector(selectAuth);
  const [dashboard, setDashboard] = useState(null);
  const [error, setError] = useState("");
  const [revenueView, setRevenueView] = useState(REVENUE_VIEWS.GRAPH);

  const load = useCallback(async () => {
    setDashboard(null);
    setError("");
    try {
      const { data } = await AdminDashboardService.summary();
      setDashboard(data || {});
    } catch (err) {
      setError(extractErrorMessage(err, "Could not load admin dashboard."));
      setDashboard({});
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const displayName = useMemo(() => getDisplayName(user), [user]);

  const usersChart = toChartData([
    { label: "Active", value: dashboard?.activeUsers },
    { label: "Suspended", value: dashboard?.suspendedUsers },
  ]);

  const kycChart = toChartData([
    { label: "Pending", value: dashboard?.pendingKyc },
    { label: "Approved", value: dashboard?.approvedKyc },
    { label: "Rejected", value: dashboard?.rejectedKyc },
  ]);

  const basicPoliciesChart = toChartData([
    { label: "Home", value: dashboard?.basicPoliciesByType?.HOME },
    { label: "Vehicle", value: dashboard?.basicPoliciesByType?.VEHICLE },
    { label: "Inactive", value: dashboard?.inactiveBasicPolicies },
  ]);

  const soldPoliciesChart = toChartData([
    { label: "Home", value: dashboard?.policiesSoldByType?.HOME },
    { label: "Vehicle", value: dashboard?.policiesSoldByType?.VEHICLE },
    { label: "Active", value: dashboard?.activePoliciesSold },
  ]);

  const claimsChart = toChartData(
    Object.entries(dashboard?.claimsByStatus || {}).map(([label, value]) => ({
      label: formatLabel(label),
      value,
    }))
  );

  const revenueSeries = toChartData([
    { label: "Gross Revenue", value: dashboard?.grossRevenue },
    { label: "Approved Payouts", value: dashboard?.totalPayouts },
    { label: "Net Revenue", value: dashboard?.totalRevenue },
  ]);

  return (
    <div className="space-y-8">
      <header className="space-y-3">
        <p className="text-xs uppercase tracking-widest text-text-subtle">Administration</p>
        <div className="space-y-1">
          <h1 className="font-heading text-4xl font-semibold">Welcome back, {displayName}</h1>
          <p className="text-text-muted">Live admin dashboard backed by current database totals.</p>
        </div>
      </header>

      {error && <ErrorAlert message={error} onRetry={load} />}

      <div className="grid sm:grid-cols-2 xl:grid-cols-3 gap-4">
        <MetricCard
          title="Users"
          icon="users"
          loading={!dashboard}
          value={dashboard?.totalUsers}
          detail={buildDetail([
            ["Active", dashboard?.activeUsers],
            ["Suspended", dashboard?.suspendedUsers],
          ])}
        />
        <MetricCard
          title="Basic Policies"
          icon="file"
          loading={!dashboard}
          value={dashboard?.totalBasicPolicies}
          detail={buildDetail([
            ["Active", dashboard?.activeBasicPolicies],
            ["Inactive", dashboard?.inactiveBasicPolicies],
            ["Home", dashboard?.basicPoliciesByType?.HOME],
            ["Vehicle", dashboard?.basicPoliciesByType?.VEHICLE],
          ])}
        />
        <MetricCard
          title="Policies Sold"
          icon="vehicle"
          loading={!dashboard}
          value={dashboard?.totalPoliciesSold}
          detail={buildDetail([
            ["Active", dashboard?.activePoliciesSold],
            ["Home", dashboard?.policiesSoldByType?.HOME],
            ["Vehicle", dashboard?.policiesSoldByType?.VEHICLE],
          ])}
        />
        <MetricCard
          title="KYC"
          icon="id-card"
          loading={!dashboard}
          value={dashboard?.totalKyc}
          detail={buildDetail([
            ["Pending", dashboard?.pendingKyc],
            ["Approved", dashboard?.approvedKyc],
            ["Rejected", dashboard?.rejectedKyc],
          ])}
        />
        <MetricCard
          title="Claims"
          icon="claims"
          loading={!dashboard}
          value={dashboard?.totalClaims}
          detail={buildDetail(
            Object.entries(dashboard?.claimsByStatus || {}).map(([label, value]) => [formatLabel(label), value])
          )}
        />
        <MetricCard
          title="Net Revenue"
          icon="revenue"
          loading={!dashboard}
          value={dashboard ? formatCurrency(asNumber(dashboard.totalRevenue)) : undefined}
          detail={buildDetail([
            ["Gross", formatCurrency(asNumber(dashboard?.grossRevenue))],
            ["Payouts", formatCurrency(asNumber(dashboard?.totalPayouts))],
          ])}
        />
      </div>

      <section className="rounded-3xl border border-border bg-surface p-6 space-y-6">
        <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
          <div>
            <p className="text-xs uppercase tracking-widest text-text-subtle">Revenue Analytics</p>
            <h2 className="font-heading text-2xl font-semibold mt-1">Revenue breakdown</h2>
          </div>
          <div className="inline-flex w-fit rounded-xl border border-border bg-surface-2 p-1">
            <ToggleButton
              active={revenueView === REVENUE_VIEWS.GRAPH}
              onClick={() => setRevenueView(REVENUE_VIEWS.GRAPH)}
            >
              Graph
            </ToggleButton>
            <ToggleButton
              active={revenueView === REVENUE_VIEWS.PIE}
              onClick={() => setRevenueView(REVENUE_VIEWS.PIE)}
            >
              Pie
            </ToggleButton>
          </div>
        </div>

        {!dashboard ? (
          <div className="grid lg:grid-cols-[1.2fr,0.8fr] gap-6">
            <Skeleton className="h-72 rounded-2xl" />
            <Skeleton className="h-72 rounded-2xl" />
          </div>
        ) : (
          <div className="grid lg:grid-cols-[1.2fr,0.8fr] gap-6">
            <div className="rounded-2xl border border-border bg-bg p-5">
              {revenueView === REVENUE_VIEWS.GRAPH ? (
                <BarChart items={revenueSeries} currency />
              ) : (
                <PieChart title="Revenue share" items={revenueSeries} currency />
              )}
            </div>
            <div className="rounded-2xl border border-border bg-bg p-5">
              <p className="text-xs uppercase tracking-widest text-text-subtle">Policy Sales Mix</p>
              <h3 className="font-heading text-xl font-semibold mt-2">Sold policy types</h3>
              <PieChart title="Policies sold" items={soldPoliciesChart} />
            </div>
          </div>
        )}
      </section>

      <section className="space-y-4">
        <div>
          <p className="text-xs uppercase tracking-widest text-text-subtle">Distribution</p>
          <h2 className="font-heading text-2xl font-semibold mt-1">Operational pie charts</h2>
        </div>

        {!dashboard ? (
          <div className="grid md:grid-cols-2 xl:grid-cols-4 gap-4">
            {Array.from({ length: 4 }).map((_, index) => (
              <Skeleton key={index} className="h-80 rounded-2xl" />
            ))}
          </div>
        ) : (
          <div className="grid md:grid-cols-2 xl:grid-cols-4 gap-4">
            <ChartCard title="Users">
              <PieChart title="User status" items={usersChart} />
            </ChartCard>
            <ChartCard title="KYC">
              <PieChart title="KYC status" items={kycChart} />
            </ChartCard>
            <ChartCard title="Basic Policies">
              <PieChart title="Basic policy split" items={basicPoliciesChart} />
            </ChartCard>
            <ChartCard title="Claims">
              <PieChart title="Claim status" items={claimsChart} />
            </ChartCard>
          </div>
        )}
      </section>
    </div>
  );
}

function getDisplayName(user) {
  if (user?.name && String(user.name).trim()) return String(user.name).trim();
  const email = String(user?.email || "").trim();
  if (!email) return "Admin";
  const localPart = email.split("@")[0] || "Admin";
  return localPart
    .replace(/[._-]+/g, " ")
    .replace(/\b\w/g, (char) => char.toUpperCase());
}

function buildDetail(entries) {
  return entries
    .filter(([, value]) => value !== undefined && value !== null && value !== "")
    .map(([label, value]) => `${label}: ${value}`)
    .join(" | ");
}

function asNumber(value) {
  const next = Number(value || 0);
  return Number.isFinite(next) ? next : 0;
}

function formatLabel(label) {
  return String(label || "")
    .toLowerCase()
    .split("_")
    .filter(Boolean)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function toChartData(entries) {
  return entries
    .map((entry, index) => ({
      label: entry.label,
      value: asNumber(entry.value),
      color: CHART_COLORS[index % CHART_COLORS.length],
    }))
    .filter((entry) => entry.value > 0);
}

function ToggleButton({ active, onClick, children }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={[
        "px-4 py-2 rounded-lg text-sm font-semibold transition-colors",
        active ? "bg-brand text-white" : "text-text-muted hover:text-text",
      ].join(" ")}
    >
      {children}
    </button>
  );
}

function MetricCard({ title, icon, loading, value, detail }) {
  return (
    <div className="rounded-2xl border border-border bg-surface p-5">
      <div className="flex items-center justify-between">
        <p className="text-xs uppercase tracking-widest text-text-subtle">{title}</p>
        <Icon name={icon} className="text-text-muted" />
      </div>
      {loading ? (
        <Skeleton className="h-8 w-24 mt-3" />
      ) : (
        <p className="font-heading text-3xl font-semibold mt-2">{value ?? 0}</p>
      )}
      {loading ? <Skeleton className="h-4 w-full mt-3" /> : <p className="mt-3 text-sm text-text-muted">{detail}</p>}
    </div>
  );
}

function ChartCard({ title, children }) {
  return (
    <div className="rounded-2xl border border-border bg-surface p-5">
      <p className="text-xs uppercase tracking-widest text-text-subtle">{title}</p>
      <div className="mt-4">{children}</div>
    </div>
  );
}

function PieChart({ title, items, currency = false }) {
  const total = items.reduce((sum, item) => sum + item.value, 0);

  if (!items.length || total <= 0) {
    return <EmptyChart title={title} />;
  }

  let currentAngle = -90;
  const segments = items.map((item) => {
    const angle = (item.value / total) * 360;
    const segment = {
      ...item,
      startAngle: currentAngle,
      endAngle: currentAngle + angle,
    };
    currentAngle += angle;
    return segment;
  });

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-center">
        <svg viewBox="0 0 120 120" className="w-48 h-48" role="img" aria-label={title}>
          <circle cx="60" cy="60" r="54" fill="#E2E8F0" />
          {segments.map((segment) => (
            <path
              key={segment.label}
              d={describeArc(60, 60, 54, segment.startAngle, segment.endAngle)}
              fill={segment.color}
            />
          ))}
          <circle cx="60" cy="60" r="28" fill="white" />
          <text x="60" y="56" textAnchor="middle" className="fill-slate-900 text-[7px] font-semibold">
            Total
          </text>
          <text x="60" y="67" textAnchor="middle" className="fill-slate-900 text-[10px] font-semibold">
            {currency ? compactCurrency(total) : total}
          </text>
        </svg>
      </div>
      <div className="space-y-2">
        {items.map((item) => (
          <div key={item.label} className="flex items-center justify-between gap-3 text-sm">
            <div className="flex items-center gap-2 min-w-0">
              <span className="w-3 h-3 rounded-full" style={{ backgroundColor: item.color }} aria-hidden="true" />
              <span className="truncate">{item.label}</span>
            </div>
            <span className="font-medium text-text-muted">
              {currency ? formatCurrency(item.value) : `${item.value} (${Math.round((item.value / total) * 100)}%)`}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}

function BarChart({ items, currency = false }) {
  const max = items.reduce((largest, item) => Math.max(largest, item.value), 0);

  if (!items.length || max <= 0) {
    return <EmptyChart title="Revenue chart" />;
  }

  return (
    <div className="space-y-5">
      <div>
        <p className="text-xs uppercase tracking-widest text-text-subtle">Live values</p>
        <h3 className="font-heading text-xl font-semibold mt-2">Gross vs payouts vs net</h3>
      </div>

      <div className="space-y-4">
        {items.map((item) => {
          const width = `${Math.max((item.value / max) * 100, 6)}%`;
          return (
            <div key={item.label} className="space-y-2">
              <div className="flex items-center justify-between gap-3 text-sm">
                <span className="font-medium">{item.label}</span>
                <span className="text-text-muted">
                  {currency ? formatCurrency(item.value) : item.value}
                </span>
              </div>
              <div className="h-3 rounded-full bg-surface-2 overflow-hidden">
                <div className="h-full rounded-full" style={{ width, backgroundColor: item.color }} />
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

function EmptyChart({ title }) {
  return (
    <div className="h-64 rounded-2xl border border-dashed border-border bg-surface-2 grid place-items-center text-center px-6">
      <div>
        <p className="font-medium">{title}</p>
        <p className="mt-1 text-sm text-text-muted">No records available yet.</p>
      </div>
    </div>
  );
}

function polarToCartesian(centerX, centerY, radius, angleInDegrees) {
  const angleInRadians = ((angleInDegrees - 90) * Math.PI) / 180;
  return {
    x: centerX + radius * Math.cos(angleInRadians),
    y: centerY + radius * Math.sin(angleInRadians),
  };
}

function describeArc(x, y, radius, startAngle, endAngle) {
  const start = polarToCartesian(x, y, radius, endAngle);
  const end = polarToCartesian(x, y, radius, startAngle);
  const largeArcFlag = endAngle - startAngle <= 180 ? "0" : "1";
  return ["M", x, y, "L", start.x, start.y, "A", radius, radius, 0, largeArcFlag, 0, end.x, end.y, "Z"].join(" ");
}

function compactCurrency(value) {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    notation: "compact",
    maximumFractionDigits: 1,
  }).format(asNumber(value));
}
