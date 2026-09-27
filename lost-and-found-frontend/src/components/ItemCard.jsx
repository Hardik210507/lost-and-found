import { Link } from "react-router-dom";
import StatusBadge from "./StatusBadge";

export default function ItemCard({ item, index }) {
  return (
    <Link
      to={`/items/${item.id}`}
      className="grid grid-cols-[34px_1fr_auto] gap-3 items-baseline px-6 py-4 border-b border-bone-dim last:border-0 hover:bg-black/5 transition-colors"
    >
      <span className="text-xs font-bold text-bone-muted">{String(index ?? item.id).padStart(2, "0")}</span>
      <div>
        <div className="font-serif-italic text-[17px] leading-tight not-italic">{item.title}</div>
        <span className="block text-[10px] text-bone-muted tracking-wide mt-0.5 uppercase">
          {item.category} · {item.location} · {new Date(item.createdAt).toLocaleDateString(undefined, { day: "2-digit", month: "short" })}
        </span>
      </div>
      <StatusBadge status={item.status} />
    </Link>
  );
}
