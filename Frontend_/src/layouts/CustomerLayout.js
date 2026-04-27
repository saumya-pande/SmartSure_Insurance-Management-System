import React, { useState } from "react";
import { Outlet } from "react-router-dom";
import {
  LayoutDashboard,
  ShoppingBag,
  FileText,
  ShieldCheck,
  FilePlus2,
} from "lucide-react";
import AppNavbar from "../components/layout/AppNavbar";
import Sidebar from "../components/layout/Sidebar";

const NAV = [
  { to: "/app", label: "Dashboard", icon: LayoutDashboard, end: true, group: "MAIN" },
  { to: "/app/policies", label: "Browse Policies", icon: ShoppingBag, group: "MAIN" },
  { to: "/app/my-policies", label: "My Policies", icon: FileText, group: "MAIN" },
  { to: "/app/claims/new", label: "File a Claim", icon: FilePlus2, group: "ACTIONS" },
  { to: "/app/kyc", label: "KYC Status", icon: ShieldCheck, group: "ACCOUNT" },
];

export default function CustomerLayout() {
  const [open, setOpen] = useState(false);
  return (
    <div className="min-h-screen flex flex-col bg-bg text-text">
      <AppNavbar onMenu={() => setOpen(true)} />
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
