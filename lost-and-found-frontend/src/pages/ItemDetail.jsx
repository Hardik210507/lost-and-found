import { useState, useEffect } from "react";
import { useParams, Link } from "react-router-dom";
import StatusBadge from "../components/StatusBadge";
import MatchCard from "../components/MatchCard";
import { fetchItemById, fetchMatchesForItem, createClaim } from "../api/apiClient";

export default function ItemDetail() {
  const { id } = useParams();
  const [item, setItem] = useState(null);
  const [matches, setMatches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [claimOpen, setClaimOpen] = useState(false);
  const [claimed, setClaimed] = useState(false);

  useEffect(() => {
    setLoading(true);
    fetchItemById(id)
      .then((data) => {
        setItem(data);
        return fetchMatchesForItem(id);
      })
      .then((m) => {
        if (m) setMatches(m);
      })
      .catch((err) => setError(err.message || "Failed to load item"))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) {
    return (
      <div className="p-16 text-center mono-label">
        Loading item record #{id}...
      </div>
    );
  }

  if (error || !item) {
    return (
      <div className="max-w-lg mx-auto px-6 py-28 text-center">
        <h2 className="font-serif-italic not-italic text-3xl mb-3">Item not found.</h2>
        <p className="text-sm text-[#4a4a4a] mb-6">{error || "Could not retrieve this item manifest."}</p>
        <Link to="/" className="px-6 py-3 border-[3px] border-ink bg-ink text-bone text-xs font-bold uppercase tracking-wide">
          ← Back to Registry
        </Link>
      </div>
    );
  }

  return (
    <div>
      <div className="grid md:grid-cols-2 border-b-[3px] border-ink">
        <div className="md:border-r-[3px] border-ink aspect-square flex items-center justify-center text-xs text-[#888] overflow-hidden bg-[#E2DDCF]">
          {item.imagePath ? (
            <img src={item.imagePath} alt={item.title} className="w-full h-full object-cover" />
          ) : (
            <span className="mono-label text-xs">No photograph attached</span>
          )}
        </div>
        <div className="p-8">
          <div className="mono-label">Item No. {String(item.id).padStart(4, "0")}</div>
          <h1 className="font-serif-italic not-italic text-4xl my-3 leading-tight">{item.title}</h1>
          <div className="flex gap-2.5 items-center mb-5">
            <StatusBadge status={item.type} />
            <StatusBadge status={item.status} />
          </div>
          <p className="text-sm text-[#4a4a4a] leading-relaxed">{item.description}</p>

          <dl className="grid grid-cols-2 gap-5 border-t-2 border-ink pt-5 my-7">
            <Meta label="Category" value={item.category} />
            <Meta label="Location" value={item.location} />
            <Meta label="Reported by" value={item.postedBy || "Anonymous"} />
            <Meta label="Date" value={item.createdAt ? new Date(item.createdAt).toLocaleDateString() : "Recent"} />
          </dl>

          {!claimed ? (
            <button
              onClick={() => setClaimOpen(true)}
              className="px-6 py-4 border-[3px] border-ink bg-ink text-bone text-xs font-bold uppercase tracking-wide hover:bg-amber hover:text-ink transition-colors"
            >
              Claim this item →
            </button>
          ) : (
            <p className="text-sm font-bold text-green-800 bg-green-100 border border-green-400 p-3">
              ✓ Claim submitted — pending admin verification.
            </p>
          )}
        </div>
      </div>

      {matches.length > 0 && (
        <div className="p-8">
          <div className="mono-label mb-1.5">Possible matches</div>
          <div className="font-serif-italic not-italic text-2xl mb-5">This might be the same item</div>
          {matches.map((m) => <MatchCard key={m.id || m.item?.id} item={m.item || m} score={m.score || 85} />)}
        </div>
      )}

      <footer className="flex justify-between items-center px-8 py-6 text-[11px] flex-wrap gap-3">
        <span>© Campus Lost &amp; Found Registry</span>
        <div className="w-10 h-[3px] bg-amber" />
        <span>Matches ranked by confidence score</span>
      </footer>

      {claimOpen && (
        <ClaimModal
          item={item}
          onClose={() => setClaimOpen(false)}
          onSuccess={() => {
            setClaimOpen(false);
            setClaimed(true);
          }}
        />
      )}
    </div>
  );
}

function Meta({ label, value }) {
  return (
    <div>
      <dt className="text-[9px] tracking-widest uppercase text-[#888] mb-1">{label}</dt>
      <dd className="text-sm font-bold">{value}</dd>
    </div>
  );
}

function ClaimModal({ item, onClose, onSuccess }) {
  const [claimant, setClaimant] = useState("");
  const [detail, setDetail] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [err, setErr] = useState(null);

  async function handleClaimSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    setErr(null);
    try {
      await createClaim({
        item: { id: item.id },
        claimant: claimant || "Verified Student",
        identifyingDetail: detail,
        status: "PENDING",
      });
      onSuccess();
    } catch (e) {
      setErr(e.message || "Failed to submit claim");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="fixed inset-0 bg-black/75 flex items-center justify-center z-50 p-5" onClick={onClose}>
      <form onSubmit={handleClaimSubmit} className="bg-bone border-[3px] border-ink max-w-md w-full p-7" onClick={(e) => e.stopPropagation()}>
        <div className="font-serif-italic not-italic text-2xl mb-3">Claim this item</div>
        <p className="text-xs text-[#4a4a4a] mb-4 leading-relaxed">
          Describe something about this item that isn't shown publicly — this helps an admin verify it's really yours.
        </p>
        {err && <p className="text-xs text-red-600 mb-2">{err}</p>}
        <input
          required
          placeholder="Your Name / Email"
          value={claimant}
          onChange={(e) => setClaimant(e.target.value)}
          className="w-full border-2 border-ink bg-bone px-3.5 py-2 text-sm mb-3 font-mono"
        />
        <textarea
          required
          rows={3}
          value={detail}
          onChange={(e) => setDetail(e.target.value)}
          placeholder="e.g. There's a scratch on the back, or specific serial number..."
          className="w-full border-2 border-ink bg-bone px-3.5 py-2.5 text-sm mb-4 resize-none font-mono"
        />
        <div className="flex gap-2.5">
          <button
            type="submit"
            disabled={submitting}
            className="flex-1 py-3 border-[3px] border-ink bg-ink text-bone text-xs font-bold uppercase tracking-wide hover:bg-amber hover:text-ink disabled:opacity-50"
          >
            {submitting ? "Submitting..." : "Submit claim"}
          </button>
          <button
            type="button"
            onClick={onClose}
            className="flex-1 py-3 border-[3px] border-ink text-xs font-bold uppercase tracking-wide"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}

