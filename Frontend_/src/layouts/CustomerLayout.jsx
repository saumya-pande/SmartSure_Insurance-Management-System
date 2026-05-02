import React, { useState } from "react";
import { Outlet } from "react-router-dom";
import AppNavbar from "../components/layout/AppNavbar";
import Sidebar from "../components/layout/Sidebar";
import MobileBottomNav from "../components/layout/MobileBottomNav";
import Icon from "../components/ui/Icon";

const NAV = [
  { to: "/app", label: "Dashboard", icon: () => <Icon name="clipboard" />, end: true, group: "MAIN" },
  { to: "/app/policies", label: "Browse Policies", icon: () => <Icon name="folder-open" />, group: "MAIN" },
  { to: "/app/my-policies", label: "My Policies", icon: () => <Icon name="file" />, group: "MAIN" },
  { to: "/app/claims", label: "My Claims", icon: () => <Icon name="clipboard" />, group: "ACTIONS" },
  { to: "/app/claims/new", label: "File a Claim", icon: () => <Icon name="plus" />, group: "ACTIONS" },
  { to: "/app/kyc", label: "KYC Status", icon: () => <Icon name="id-card" />, group: "ACCOUNT" },
  { to: "/app/profile", label: "Profile", icon: () => <Icon name="user" />, group: "ACCOUNT" },
];

const BOTTOM_NAV = [
  { to: "/app", label: "Dashboard", icon: () => <Icon name="clipboard" />, end: true },
  { to: "/app/policies", label: "Bazaar", icon: () => <Icon name="folder-open" /> },
  { to: "/app/claims", label: "Claims", icon: () => <Icon name="claims" /> },
  { to: "/app/profile", label: "Profile", icon: () => <Icon name="user" /> },
];

export default function CustomerLayout() {
  const [open, setOpen] = useState(false);

  return (
    <div className="h-screen overflow-hidden flex flex-col bg-bg text-text">
      <AppNavbar onMenu={() => setOpen(true)} />
      <div className="flex-1 min-h-0 flex overflow-hidden">
        <Sidebar items={NAV} open={open} onClose={() => setOpen(false)} title="Customer Navigation" />
        <main className="flex-1 min-w-0 overflow-y-auto px-3 sm:px-4 lg:px-6 py-5 sm:py-6 pb-24 lg:pb-6">
          <div className="mx-auto w-full max-w-[1600px]">
            <Outlet />
          </div>
        </main>
      </div>
      <MobileBottomNav items={BOTTOM_NAV} />
    </div>
  );
}
