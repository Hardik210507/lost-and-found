import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  const linkClass = ({ isActive }) =>
    `text-xs font-medium transition-colors ${isActive ? "text-amber underline underline-offset-4" : "text-ink hover:text-amber"}`;

  const handleLogout = () => {
    logout();
    navigate("/auth");
  };

  return (
    <header className="sticky top-0 z-50 bg-bone border-b-[3px] border-ink">
      <div className="px-8 py-5 flex items-center justify-between flex-wrap gap-4">
        <Link to={isAdmin ? "/admin" : user ? "/" : "/auth"} className="font-serif-italic not-italic text-xl">
          Lost &amp; Found {isAdmin && <span className="text-amber not-italic text-sm font-sans font-bold ml-2">/ Admin Console</span>}
        </Link>

        {/* Only show nav links when authenticated */}
        {user && (
          <>
            {/* Regular student navigation (hidden for admin) */}
            {!isAdmin && (
              <nav className="flex items-center gap-6 flex-wrap">
                <NavLink to="/" end className={linkClass}>Manifest</NavLink>
                <NavLink to="/post" className={linkClass}>Report</NavLink>
                <NavLink to="/my-items" className={linkClass}>My items</NavLink>
              </nav>
            )}

            {/* Admin only badge when logged in as admin */}
            {isAdmin && (
              <div className="mono-label text-[11px] font-bold text-amber border-2 border-amber bg-ink px-3 py-1">
                Admin View Active
              </div>
            )}
          </>
        )}

        <div className="flex items-center gap-4">
          {user ? (
            <div className="flex items-center gap-3">
              <div
                title={`${user.name} (${user.role})`}
                className={`w-[34px] h-[34px] border-2 border-ink flex items-center justify-center text-xs font-bold ${
                  isAdmin ? "bg-amber text-ink" : "bg-ink text-bone"
                }`}
              >
                {isAdmin ? "A" : "S"}
              </div>
              <span className="text-xs font-mono hidden sm:inline">{user.name.split(" ")[0]}</span>
              <button
                onClick={handleLogout}
                className="px-2.5 py-1 text-[11px] font-bold uppercase border-2 border-ink hover:bg-black hover:text-white transition-colors"
              >
                Log out
              </button>
            </div>
          ) : (
            <Link
              to="/auth"
              className="px-4 py-1.5 text-xs font-bold uppercase tracking-wide border-2 border-ink bg-ink text-bone hover:bg-amber hover:text-ink transition-colors"
            >
              Sign In
            </Link>
          )}
        </div>
      </div>
    </header>
  );
}
