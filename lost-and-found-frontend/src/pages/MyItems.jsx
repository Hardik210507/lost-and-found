import { useState, useEffect } from "react";
import ItemCard from "../components/ItemCard";
import StatusBadge from "../components/StatusBadge";
import { fetchItems, fetchClaims } from "../api/apiClient";

const TABS = ["Lost items", "Found items", "Claims"];

export default function MyItems() {
  const [tab, setTab] = useState(0);
  const [items, setItems] = useState([]);
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([fetchItems(), fetchClaims()])
      .then(([itemsData, claimsData]) => {
        setItems(itemsData || []);
        setClaims(claimsData || []);
      })
      .catch((err) => console.error("Error fetching data:", err))
      .finally(() => setLoading(false));
  }, []);

  const lost = items.filter((i) => i.type === "LOST");
  const found = items.filter((i) => i.type === "FOUND");

  return (
    <div>
      <div className="px-8 pt-12 pb-8 border-b-[3px] border-ink">
        <div className="mono-label mb-2">Your board</div>
        <h1 className="font-serif-italic not-italic text-5xl">My items.</h1>
      </div>

      <div className="flex border-b-[3px] border-ink">
        {TABS.map((t, i) => (
          <button
            key={t}
            onClick={() => setTab(i)}
            className={`px-6 py-4 text-[11px] font-bold uppercase tracking-wide border-r-2 border-ink last:border-r-0 ${tab === i ? "bg-ink text-bone" : ""}`}
          >
            {t}
          </button>
        ))}
      </div>

      <div className="p-8 max-w-3xl">
        {loading ? (
          <div className="mono-label py-8 text-center">Loading board...</div>
        ) : (
          <>
            {tab === 0 && <ItemRows items={lost} />}
            {tab === 1 && <ItemRows items={found} />}
            {tab === 2 && <ClaimRows claims={claims} />}
          </>
        )}
      </div>

      <footer className="flex justify-between items-center px-8 py-6 text-[11px] flex-wrap gap-3">
        <span>© Campus Lost &amp; Found Registry</span>
        <div className="w-10 h-[3px] bg-amber" />
        <span>Status updates in real time</span>
      </footer>
    </div>
  );
}

function ItemRows({ items }) {
  if (items.length === 0) return <Empty text="Nothing reported yet." />;
  return (
    <div className="border-2 border-ink">
      {items.map((item, i) => <ItemCard key={item.id} item={item} index={i + 1} />)}
    </div>
  );
}

function ClaimRows({ claims }) {
  if (claims.length === 0) return <Empty text="No claims submitted yet." />;
  return (
    <div className="space-y-3">
      {claims.map((c) => (
        <div key={c.id} className="border-2 border-ink p-5">
          <div className="flex justify-between items-center">
            <span className="font-serif-italic not-italic text-lg">{c.item.title}</span>
            <StatusBadge status="PENDING" />
          </div>
          <p className="text-xs text-[#4a4a4a] mt-1">Awaiting admin verification</p>
        </div>
      ))}
    </div>
  );
}

function Empty({ text }) {
  return <div className="text-center py-16 text-sm text-bone-muted border-2 border-dashed border-ink">{text}</div>;
}
