import { useState } from "react";
import { useNavigate } from "react-router-dom";

export default function Auth() {
  const [mode, setMode] = useState("login");
  const navigate = useNavigate();

  return (
    <div>
      <div className="text-center px-8 pt-12 pb-6 border-b-[3px] border-ink">
        <div className="mono-label">Campus account</div>
      </div>

      <div className="max-w-sm mx-auto px-5 py-14">
        <div className="font-serif-italic not-italic text-4xl text-center mb-8">
          {mode === "login" ? "Welcome back." : "Create an account."}
        </div>

        <form onSubmit={(e) => { e.preventDefault(); navigate("/"); }}>
          {mode === "register" && (
            <Field label="Full name"><input required className="input" /></Field>
          )}
          <Field label="College email"><input required type="email" className="input" /></Field>
          <Field label="Password"><input required type="password" className="input" /></Field>
          <button type="submit" className="w-full py-4 border-[3px] border-ink bg-ink text-bone text-xs font-bold uppercase tracking-wide hover:bg-amber hover:text-ink transition-colors">
            {mode === "login" ? "Log in →" : "Sign up →"}
          </button>
        </form>

        <p className="text-center text-[11px] text-[#4a4a4a] mt-5">
          {mode === "login" ? "New here?" : "Already have an account?"}{" "}
          <button onClick={() => setMode(mode === "login" ? "register" : "login")} className="underline font-bold text-ink">
            {mode === "login" ? "Create one" : "Log in"}
          </button>
        </p>
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
