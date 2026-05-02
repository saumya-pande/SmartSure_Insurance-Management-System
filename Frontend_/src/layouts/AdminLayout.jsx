import React, { useState } from "react";
import { Outlet } from "react-router-dom";
import AppNavbar from "../components/layout/AppNavbar";
import Sidebar from "../components/layout/Sidebar";
import Icon from "../components/ui/Icon";

const NAV = [
  { to: "/admin", label: "Overview", icon: () => <Icon name="clipboard" />, end: true, group: "MAIN" },
  { to: "/admin/policies", label: "Policies", icon: () => <Icon name="file" />, group: "MAIN" },
  { to: "/admin/purchases", label: "Purchased Policies", icon: () => <Icon name="wallet" />, group: "MAIN" },
  { to: "/admin/claims", label: "Claims", icon: () => <Icon name="clipboard" />, group: "OPERATIONS" },
  { to: "/admin/kyc", label: "KYC Management", icon: () => <Icon name="id-card" />, group: "OPERATIONS" },
  { to: "/admin/users", label: "Users", icon: () => <Icon name="users" />, group: "OPERATIONS" },
];

export default function AdminLayout() {
  const [open, setOpen] = useState(false);

  return (
    <div className="h-screen overflow-hidden flex flex-col bg-bg text-text">
      <AppNavbar onMenu={() => setOpen(true)} admin />
      <div className="flex-1 min-h-0 flex overflow-hidden">
        <Sidebar items={NAV} open={open} onClose={() => setOpen(false)} />
        <main className="flex-1 min-w-0 overflow-y-auto px-3 sm:px-4 lg:px-6 py-5 sm:py-6">
          <div className="mx-auto w-full max-w-[1600px]">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}
