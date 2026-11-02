const STYLES = {
  PENDING: "bg-transparent text-ink border-ink",
  MATCHED: "bg-ink text-bone border-ink",
  CLAIMED: "bg-ink text-bone border-ink",
  RESOLVED: "bg-amber text-ink border-ink",
  UNRESOLVED: "bg-red-100 text-red-700 border-red-400",
};

const LABELS = {
  PENDING: "Pending",
  MATCHED: "Matched",
  CLAIMED: "Claimed",
  RESOLVED: "Resolved",
  UNRESOLVED: "Unresolved",
};

export default function StatusBadge({ status }) {
  return (
    <span
      className={`inline-block px-2 py-0.5 text-[9px] font-bold tracking-widest uppercase border-[1.5px] whitespace-nowrap ${STYLES[status] || STYLES.PENDING}`}
    >
      {LABELS[status] || status}
    </span>
  );
}
