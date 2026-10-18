import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { loginUser, registerUser } from "../api/apiClient";

export default function Auth() {
  const [mode, setMode] = useState("login");
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState("USER"); // "USER" or "ADMIN"
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      let userData;
      if (mode === "login") {
        userData = await loginUser({ email, password });
      } else {
        userData = await registerUser({ name, email, password, role });
      }

      login(userData);

      // Redirection logic based on role
      if (userData.role === "ADMIN") {
        navigate("/admin");
      } else {
        navigate("/");
      }
    } catch (err) {
      setError(err.message || "Authentication failed");
    } finally {
      setLoading(false);
    }
  };

  const fillDemoAdmin = () => {
    setMode("login");
    setEmail("admin@campus.edu");
    setPassword("admin123");
    setError(null);
  };

  return (
    <div>
      <div className="text-center px-8 pt-12 pb-6 border-b-[3px] border-ink">
        <div className="mono-label">Campus Property Registry</div>
      </div>

      <div className="max-w-sm mx-auto px-5 py-12">
        <div className="font-serif-italic not-italic text-4xl text-center mb-8">
          {mode === "login" ? "Welcome back." : "Create an account."}
        </div>

        {error && (
          <div className="p-3 mb-6 bg-red-100 border-2 border-red-500 text-red-800 text-xs font-mono">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          {mode === "register" && (
            <>
              <Field label="Full name">
                <input
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="e.g. Hardik Shah"
                  className="input"
                />
              </Field>
              <Field label="Account Type">
                <select
                  value={role}
                  onChange={(e) => setRole(e.target.value)}
                  className="input cursor-pointer"
                >
                  <option value="USER">Student / Regular User</option>
                  <option value="ADMIN">Campus Administrator</option>
                </select>
              </Field>
            </>
          )}

          <Field label="College email">
            <input
              required
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="e.g. student@campus.edu"
              className="input"
            />
          </Field>

          <Field label="Password">
            <input
              required
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              className="input"
            />
          </Field>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-4 border-[3px] border-ink bg-ink text-bone text-xs font-bold uppercase tracking-wide hover:bg-amber hover:text-ink transition-colors disabled:opacity-50"
          >
            {loading ? "Processing..." : mode === "login" ? "Log in →" : "Sign up →"}
          </button>
        </form>

        <p className="text-center text-[11px] text-[#4a4a4a] mt-5">
          {mode === "login" ? "New here?" : "Already have an account?"}{" "}
          <button
            type="button"
            onClick={() => {
              setMode(mode === "login" ? "register" : "login");
              setError(null);
            }}
            className="underline font-bold text-ink hover:text-amber"
          >
            {mode === "login" ? "Create one" : "Log in"}
          </button>
        </p>

        {/* Quick Demo Helper */}
        <div className="mt-8 pt-6 border-t border-ink/20 text-center">
          <p className="mono-label text-[10px] text-bone-muted mb-2">Pre-seeded Admin Access:</p>
          <button
            type="button"
            onClick={fillDemoAdmin}
            className="text-[11px] font-bold uppercase tracking-wide px-3 py-1.5 border border-ink hover:bg-amber hover:text-ink transition-colors"
          >
            Fill Demo Admin Credentials
          </button>
        </div>
      </div>

      <style>{`
        .input {
          width: 100%;
          border: 2.5px solid #141414;
          background: #EDE9DD;
          padding: 12px 14px;
          font-family: 'JetBrains Mono', monospace;
          font-size: 13px;
          margin-bottom: 18px;
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
