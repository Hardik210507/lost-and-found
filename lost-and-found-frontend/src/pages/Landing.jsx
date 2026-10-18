import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { motion } from "motion/react";
import ItemCard from "../components/ItemCard";
import { categories, locations } from "../data/mockItems";
import { fetchItems } from "../api/apiClient";

const MotionLink = motion.create(Link);

export default function Landing() {
  const [filters, setFilters] = useState({ category: "", location: "", type: "" });
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchItems()
      .then((data) => setItems(data))
      .catch((err) => console.error("Failed to fetch items:", err))
      .finally(() => setLoading(false));
  }, []);

  const filtered = items.filter((item) => {
    if (filters.category && item.category !== filters.category) return false;
    if (filters.location && item.location !== filters.location) return false;
    if (filters.type && item.type !== filters.type) return false;
    return true;
  });

  return (
    <div>
      {/* Hero */}
      <section className="grid md:grid-cols-[1.3fr_1fr] border-b-[3px] border-ink">
        <div className="relative px-8 pt-16 pb-24 md:border-r-[3px] border-ink">
          <div className="mono-label mb-4">
            Campus property registry — <span className="text-amber">{items.length} active record{items.length === 1 ? "" : "s"}</span>
          </div>
          <h1 className="font-serif-italic not-italic text-[13vw] md:text-7xl leading-[0.92] tracking-tight">
            You lost it.
            <br />
            Someone <em className="text-amber">found</em> it.
            <br />
            Now prove it's yours.
          </h1>
          <p className="max-w-md text-sm leading-relaxed text-[#3a3a3a] mt-7">
            No group chats. No lost-property drawer nobody checks. Log what's
            missing or what you picked up, and the system runs the match
            itself — category, location, even the photo.
          </p>
          <div className="flex flex-wrap gap-0 mt-10">
            <MotionLink
              to="/post?type=lost"
              whileHover={{ backgroundColor: "#F4C10F", color: "#141414" }}
              transition={{ duration: 0.15 }}
              className="px-6 py-4 border-[3px] border-ink bg-ink text-bone text-xs font-bold tracking-wide uppercase"
            >
              File a loss report →
            </MotionLink>
            <MotionLink
              to="/post?type=found"
              whileHover={{ backgroundColor: "#141414", color: "#EDE9DD" }}
              transition={{ duration: 0.15 }}
              className="px-6 py-4 border-[3px] border-l-0 border-ink text-xs font-bold tracking-wide uppercase"
            >
              I found something
            </MotionLink>
          </div>
        </div>

        {/* Live manifest, replaces a generic hero visual with real data */}
        <div className="flex flex-col">
          <div className="flex justify-between items-baseline px-6 py-5 border-b-[3px] border-ink">
            <span className="mono-label text-[#3a3a3a]">Live manifest</span>
            <span className="text-xs font-bold text-amber">{items.length} open entries</span>
          </div>
          {items.slice(0, 4).map((item, i) => (
            <ItemCard key={item.id} item={item} index={i + 1} />
          ))}
        </div>
      </section>

      {/* Filters + full manifest */}
      <section className="px-8 py-10 border-b-[3px] border-ink">
        <div className="flex items-end justify-between flex-wrap gap-4 mb-6">
          <div>
            <div className="mono-label mb-2">On the shelf</div>
            <h2 className="font-serif-italic not-italic text-2xl">Recently reported</h2>
          </div>
          <div className="flex gap-2 flex-wrap">
            {[
              { key: "type", opts: ["LOST", "FOUND"], placeholder: "All types" },
              { key: "category", opts: categories, placeholder: "All categories" },
              { key: "location", opts: locations, placeholder: "All locations" },
            ].map((f) => (
              <select
                key={f.key}
                value={filters[f.key]}
                onChange={(e) => setFilters((prev) => ({ ...prev, [f.key]: e.target.value }))}
                className="border-2 border-ink bg-bone px-3 py-2 text-[11px] font-bold uppercase tracking-wide"
              >
                <option value="">{f.placeholder}</option>
                {f.opts.map((o) => <option key={o} value={o}>{o}</option>)}
              </select>
            ))}
          </div>
        </div>

        <div className="border-2 border-ink">
          {filtered.length === 0 ? (
            <p className="text-sm text-bone-muted p-8">No items match those filters yet.</p>
          ) : (
            filtered.map((item, i) => (
              <motion.div
                key={item.id}
                initial={{ opacity: 0, y: 10 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true, margin: "-40px" }}
                transition={{ duration: 0.3, delay: i * 0.05 }}
              >
                <ItemCard item={item} index={i + 1} />
              </motion.div>
            ))
          )}
        </div>
      </section>

      {/* How it works */}
      <section className="grid md:grid-cols-[0.6fr_1.4fr] border-b-[3px] border-ink">
        <div className="px-8 py-14 md:border-r-[3px] border-ink">
          <div className="mono-label mb-3">The process</div>
          <div className="font-serif-italic not-italic text-4xl leading-none">
            Three steps.
            <br />
            No committee.
          </div>
        </div>
        <div>
          {[
            { n: "01", title: "Log it", body: "Lost or found — title, category, where it happened, a photo if you've got one. Takes under a minute." },
            { n: "02", title: "It gets scored", body: "Every new entry is checked against the opposite list — category, location, wording, even image similarity — and ranked by confidence." },
            { n: "03", title: "Claim, verify, done", body: "You submit a detail only the real owner would know. An admin checks it before anything's handed back — no false claims." },
          ].map((step, i, arr) => (
            <div
              key={step.n}
              className={`grid grid-cols-[70px_1fr] md:grid-cols-[90px_1fr] gap-6 px-8 py-10 ${i !== arr.length - 1 ? "border-b-2 border-ink" : ""}`}
            >
              <div className="font-serif-italic not-italic text-5xl text-amber leading-none">{step.n}</div>
              <div>
                <h3 className="font-serif-italic not-italic text-2xl mb-2">{step.title}</h3>
                <p className="text-sm text-[#4a4a4a] leading-relaxed max-w-lg">{step.body}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      <footer className="flex justify-between items-center px-8 py-6 text-[11px] flex-wrap gap-3">
        <span>© Campus Lost &amp; Found Registry</span>
        <div className="w-10 h-[3px] bg-amber" />
        <span>Built for students, not for show</span>
      </footer>
    </div>
  );
}
