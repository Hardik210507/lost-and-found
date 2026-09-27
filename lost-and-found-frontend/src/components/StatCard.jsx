export default function StatCard({ label, value, suffix = "" }) {
  return (
    <div className="p-6 border-r-2 border-ink last:border-r-0">
      <div className="mono-label text-bone-muted mb-2">{label}</div>
      <div className="font-serif-italic not-italic text-4xl leading-none">
        {value}
        {suffix && <span className="text-base text-bone-muted ml-1">{suffix}</span>}
      </div>
    </div>
  );
}
