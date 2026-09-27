import { useState } from "react";
import { Link, NavLink } from "react-router-dom";
import { mockNotifications } from "../data/mockItems";

export default function Navbar({ role = "STUDENT" }) {
  const [notifOpen, setNotifOpen] = useState(false);
  const unreadCount = mockNotifications.filter((n) => !n.isRead).length;

  const linkClass = ({ isActive }) =>
    `text-xs font-medium transition-colors ${isActive ? "text-amber underline underline-offset-4" : "text-ink hover:text-amber"}`;

  return (
    <header className="sticky top-0 z-50 bg-bone border-b-[3px] border-ink">
      <div className="px-8 py-5 flex items-center justify-between flex-wrap gap-4">
        <Link to="/" className="font-serif-italic not-italic text-xl">Lost &amp; Found</Link>

        <nav className="flex items-center gap-6 flex-wrap">
          <NavLink to="/" end className={linkClass}>Manifest</NavLink>
          <NavLink to="/post" className={linkClass}>Report</NavLink>
          <NavLink to="/my-items" className={linkClass}>My items</NavLink>
          {role === "ADMIN" && <NavLink to="/admin" className={linkClass}>Admin</NavLink>}
        </nav>

        <div className="flex items-center gap-4">
          <div className="relative">
            <button
              onClick={() => setNotifOpen((v) => !v)}
              className="relative w-[34px] h-[34px] border-2 border-ink flex items-center justify-center text-sm"
              aria-label="Notifications"
            >
              ✉
              {unreadCount > 0 && (
                <span className="absolute -top-1 -right-1 w-2 h-2 bg-amber border border-ink" />
              )}
            </button>
            {notifOpen && (
              <div className="absolute right-0 mt-2 w-72 bg-bone border-2 border-ink shadow-xl">
                {mockNotifications.map((n) => (
                  <div key={n.id} className="px-4 py-3 text-xs border-b border-bone-dim last:border-0">
                    {n.message}
                  </div>
                ))}
              </div>
            )}
          </div>
          <div className="w-[34px] h-[34px] border-2 border-ink flex items-center justify-center text-xs font-bold">
            {role === "ADMIN" ? "A" : "S"}
          </div>
        </div>
      </div>
    </header>
  );
}
