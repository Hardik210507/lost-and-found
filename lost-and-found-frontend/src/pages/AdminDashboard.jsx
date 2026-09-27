import { useState, useEffect } from "react";
import StatCard from "../components/StatCard";
import StatusBadge from "../components/StatusBadge";
import { fetchItems, fetchClaims, updateClaimStatus } from "../api/apiClient";

export default function AdminDashboard() {
  const [items, setItems] = useState([]);
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);

  const loadData = () => {
    Promise.all([fetchItems(), fetchClaims()])
      .then(([itemsData, claimsData]) => {
        setItems(itemsData || []);
        setClaims(claimsData || []);
      })
      .catch((err) => console.error("Admin dashboard fetch error:", err))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleStatusChange = async (claimId, newStatus) => {
    try {
      await updateClaimStatus(claimId, newStatus);
      setClaims((prev) =>
        prev.map((c) => (c.id === claimId ? { ...c, status: newStatus } : c))
      );
    } catch (e) {
      alert("Failed to update status: " + e.message);
    }
  };

  const totalItems = items.length;
  const resolvedCount = items.filter((i) => i.status === "RESOLVED").length;
  const pendingCount = items.filter((i) => i.status === "PENDING").length;
  const lostCount = items.filter((i) => i.type === "LOST").length;
  const foundCount = items.filter((i) => i.type === "FOUND").length;
  const maxCount = Math.max(lostCount, foundCount, 1);
  const pendingClaims = claims.filter((c) => c.status === "PENDING");

  return (
    <div>
      <div className="px-8 pt-12 pb-8 border-b-[3px] border-ink">
        <div className="mono-label mb-2">Overview</div>
        <h1 className="font-serif-italic not-italic text-5xl">Admin dashboard.</h1>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 border-b-[3px] border-ink">
        <StatCard label="Total items" value={totalItems} />
        <StatCard label="Resolved" value={resolvedCount} />
        <StatCard label="Pending" value={pendingCount} />
        <StatCard label="Active Claims" value={claims.length} />
      </div>

      <div className="px-8 py-8">
        <div className="mono-label mb-4">Lost vs found</div>
        <Bar label="Lost" value={lostCount} max={maxCount} color="bg-ink" />
        <Bar label="Found" value={foundCount} max={maxCount} color="bg-amber" />

        <div className="mono-label mt-9 mb-4">Pending claims</div>
        {loading ? (
          <p className="text-sm text-bone-muted py-4">Loading claims...</p>
        ) : pendingClaims.length === 0 ? (
          <p className="text-sm text-bone-muted py-4">No pending claims right now.</p>
        ) : (
          pendingClaims.map((c) => (
            <div key={c.id} className="border-2 border-ink p-5 mb-3.5">
              <div className="flex justify-between mb-2">
                <span className="font-serif-italic not-italic text-lg">{c.item?.title || "Item #" + c.item?.id}</span>
                <span className="mono-label text-[#888] font-medium">Claimed by {c.claimant}</span>
              </div>
              <p className="text-xs italic text-[#4a4a4a] border-l-[3px] border-amber pl-3 my-2.5">"{c.identifyingDetail}"</p>
              <div className="flex gap-2.5">
                <button
                  onClick={() => handleStatusChange(c.id, "APPROVED")}
                  className="px-4 py-2 bg-amber border-2 border-ink text-xs font-bold uppercase tracking-wide hover:bg-black hover:text-white transition-colors"
                >
                  Approve
                </button>
                <button
                  onClick={() => handleStatusChange(c.id, "REJECTED")}
                  className="px-4 py-2 border-2 border-ink text-xs font-bold uppercase tracking-wide hover:bg-red-600 hover:text-white transition-colors"
                >
                  Reject
                </button>
              </div>
            </div>
          ))
        )}

        <div className="mono-label mt-9 mb-4">All items ({items.length})</div>
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left text-[10px] uppercase tracking-wide text-[#4a4a4a] border-b-[3px] border-ink">
              <th className="py-3 px-4 font-normal">Title</th>
              <th className="py-3 px-4 font-normal">Type</th>
              <th className="py-3 px-4 font-normal">Category</th>
              <th className="py-3 px-4 font-normal">Status</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id} className="border-b border-bone-dim last:border-0">
                <td className="py-3.5 px-4">{item.title}</td>
                <td className="py-3.5 px-4">{item.type}</td>
                <td className="py-3.5 px-4">{item.category}</td>
                <td className="py-3.5 px-4"><StatusBadge status={item.status} /></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <footer className="flex justify-between items-center px-8 py-6 text-[11px] flex-wrap gap-3">
        <span>© Campus Lost &amp; Found Registry</span>
        <div className="w-10 h-[3px] bg-amber" />
        <span>Admin access only</span>
      </footer>
    </div>
  );
}

function Bar({ label, value, max, color }) {
  return (
    <div className="flex items-center gap-3 mb-3.5 text-[11px]">
      <span className="w-12">{label}</span>
      <div className="flex-1 h-3.5 bg-bone-dim border-2 border-ink">
        <div className={`h-full ${color}`} style={{ width: `${(value / max) * 100}%` }} />
      </div>
      <span className="w-6 text-right">{value}</span>
    </div>
  );
}
