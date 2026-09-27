import { Link } from "react-router-dom";

export default function MatchCard({ item, score }) {
  return (
    <Link
      to={`/items/${item.id}`}
      className="grid grid-cols-[60px_1fr_auto] gap-4 items-center border-2 border-ink p-4 mb-3 hover:bg-black/5 transition-colors"
    >
      <div className="border-2 border-ink aspect-square flex items-center justify-center text-[9px] font-bold">
        {item.type}
      </div>
      <div>
        <div className="font-serif-italic not-italic text-base">{item.title}</div>
        <div className="mono-label text-bone-muted font-medium mt-0.5">{item.category} · {item.location}</div>
        <div className="h-1.5 bg-bone-dim mt-1.5">
          <div className="h-full bg-amber" style={{ width: `${score}%` }} />
        </div>
      </div>
      <div className="font-serif-italic not-italic text-2xl text-amber text-right">{score}%</div>
    </Link>
  );
}
