import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { categories, locations } from "../data/mockItems";
import { createItem } from "../api/apiClient";
import { useAuth } from "../context/AuthContext";

export default function PostItem() {
  const { user } = useAuth();
  const [params] = useSearchParams();
  const [type, setType] = useState(params.get("type") === "found" ? "FOUND" : "LOST");
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [category, setCategory] = useState("");
  const [location, setLocation] = useState("");
  const [postedBy, setPostedBy] = useState(user?.name || "Student User");
  const [preview, setPreview] = useState(null);
  const [submitted, setSubmitted] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState(null);

  function handleImage(e) {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (event) => {
        const img = new Image();
        img.onload = () => {
          // Resize to max 800px width/height while keeping aspect ratio
          const canvas = document.createElement("canvas");
          let width = img.width;
          let height = img.height;
          const maxDim = 800;

          if (width > height && width > maxDim) {
            height = Math.round((height * maxDim) / width);
            width = maxDim;
          } else if (height > maxDim) {
            width = Math.round((width * maxDim) / height);
            height = maxDim;
          }

          canvas.width = width;
          canvas.height = height;
          const ctx = canvas.getContext("2d");
          ctx.drawImage(img, 0, 0, width, height);

          // Compress to clean JPEG (~50-80KB)
          const optimizedDataUrl = canvas.toDataURL("image/jpeg", 0.75);
          setPreview(optimizedDataUrl);
        };
        img.src = event.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setIsSubmitting(true);
    setError(null);
    try {
      await createItem({
        type,
        title,
        description,
        category,
        location,
        postedBy: postedBy || "Anonymous User",
        imagePath: preview || null,
        status: "PENDING",
      });
      setSubmitted(true);
    } catch (err) {
      setError(err.message || "Failed to submit item");
    } finally {
      setIsSubmitting(false);
    }
  }

  if (submitted) {
    return (
      <div className="max-w-lg mx-auto px-6 py-28 text-center">
        <div className="w-12 h-12 border-2 border-ink flex items-center justify-center mx-auto mb-5 bg-amber font-bold">✓</div>
        <h2 className="font-serif-italic not-italic text-3xl mb-3">Entry filed.</h2>
        <p className="text-sm text-[#4a4a4a]">We're checking it against everything else on the manifest. You'll get a notification if a match turns up.</p>
      </div>
    );
  }

  return (
    <div>
      <div className="px-8 pt-12 pb-8 border-b-[3px] border-ink">
        <div className="mono-label mb-2">New entry</div>
        <h1 className="font-serif-italic not-italic text-5xl">Log an item.</h1>
      </div>

      <form
        onSubmit={handleSubmit}
        className="grid md:grid-cols-2 border-b-[3px] border-ink"
      >
        <div className="p-8 md:border-r-[3px] border-ink">
          {error && (
            <div className="p-3 mb-4 bg-red-100 border-2 border-red-500 text-red-700 text-xs font-mono">
              {error}
            </div>
          )}
          <div className="flex w-fit border-[3px] border-ink mb-8">
            <button type="button" onClick={() => setType("LOST")} className={`px-6 py-3 text-xs font-bold uppercase tracking-wide ${type === "LOST" ? "bg-ink text-bone" : ""}`}>Lost</button>
            <button type="button" onClick={() => setType("FOUND")} className={`px-6 py-3 text-xs font-bold uppercase tracking-wide ${type === "FOUND" ? "bg-ink text-bone" : ""}`}>Found</button>
          </div>

          <Field label="Title">
            <input
              required
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. Black leather wallet"
              className="input"
            />
          </Field>
          <Field label="Description">
            <textarea
              required
              rows={4}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Any identifying details..."
              className="input resize-y"
            />
          </Field>
          <div className="grid grid-cols-2 gap-4">
            <Field label="Category">
              <select
                required
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="input"
              >
                <option value="">Select</option>
                {categories.map((c) => <option key={c} value={c}>{c}</option>)}
              </select>
            </Field>
            <Field label="Location">
              <select
                required
                value={location}
                onChange={(e) => setLocation(e.target.value)}
                className="input"
              >
                <option value="">Select</option>
                {locations.map((l) => <option key={l} value={l}>{l}</option>)}
              </select>
            </Field>
          </div>
          <Field label="Reported By">
            <input
              value={postedBy}
              onChange={(e) => setPostedBy(e.target.value)}
              placeholder="Your name or roll number"
              className="input"
            />
          </Field>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full py-4 border-[3px] border-ink bg-ink text-bone text-xs font-bold uppercase tracking-wide mt-2 hover:bg-amber hover:text-ink transition-colors disabled:opacity-50"
          >
            {isSubmitting ? "Submitting..." : "Submit entry →"}
          </button>
        </div>

        <div className="p-8 flex items-center justify-center">
          <div className="border-[3px] border-ink p-6 w-full max-w-xs">
            <div className="mono-label text-bone-muted mb-4">New Entry — Live Preview</div>
            <div className="aspect-[4/3] border-2 border-dashed border-ink flex items-center justify-center text-xs text-[#888] mb-3.5 overflow-hidden">
              {preview ? <img src={preview} alt="preview" className="w-full h-full object-cover" /> : "Photo will appear here"}
            </div>
            <label className="block border-2 border-dashed border-ink p-6 text-center text-xs text-[#4a4a4a] cursor-pointer">
              Click to attach a photo (optional)
              <input type="file" accept="image/*" onChange={handleImage} className="hidden" />
            </label>
          </div>
        </div>
      </form>

      <footer className="flex justify-between items-center px-8 py-6 text-[11px] flex-wrap gap-3">
        <span>© Campus Lost &amp; Found Registry</span>
        <div className="w-10 h-[3px] bg-amber" />
        <span>Entries reviewed by category match</span>
      </footer>

      <style>{`
        .input {
          width: 100%;
          border: 2.5px solid #141414;
          background: #EDE9DD;
          padding: 12px 14px;
          font-family: 'JetBrains Mono', monospace;
          font-size: 13px;
          margin-bottom: 22px;
        }
        .input:focus { outline: none; }
      `}</style>
    </div>
  );
}

function Field({ label, children }) {
  return (
    <div>
      <label className="block text-[10px] tracking-widest uppercase font-bold mb-2 text-[#4a4a4a]">{label}</label>
      {children}
    </div>
  );
}
