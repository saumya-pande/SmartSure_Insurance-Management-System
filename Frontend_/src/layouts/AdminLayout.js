import React, { useState } from "react";
import { Outlet } from "react-router-dom";
import {
  LayoutDashboard,
  Users,
  FileText,
  ShieldCheck,
  ClipboardList,
} from "lucide-react";
import AppNavbar from "../components/layout/AppNavbar";
import Sidebar from "../components/layout/Sidebar";

const NAV = [
  { to: "/admin", label: "Overview", icon: LayoutDashboard, end: true, group: "MAIN" },
  { to: "/admin/policies", label: "Policies", icon: FileText, group: "MAIN" },
  { to: "/admin/claims", label: "Claims", icon: ClipboardList, group: "OPERATIONS" },
  { to: "/admin/kyc", label: "KYC Management", icon: ShieldCheck, group: "OPERATIONS" },
  { to: "/admin/users", label: "Users", icon: Users, group: "OPERATIONS" },
];

export default function AdminLayout() {
  const [open, setOpen] = useState(false);
  return (
    <div className="min-h-screen flex flex-col bg-bg text-text">
      <AppNavbar onMenu={() => setOpen(true)} admin />
      <div className="flex-1 flex">
        <Sidebar items={NAV} open={open} onClose={() => setOpen(false)} />
        <main className="flex-1 min-w-0 px-4 sm:px-8 py-8">
          <div className="max-w-6xl mx-auto">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}
