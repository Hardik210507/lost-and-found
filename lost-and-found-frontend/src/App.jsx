import { BrowserRouter, Routes, Route, useLocation, Navigate, Link } from "react-router-dom";
import { AnimatePresence } from "motion/react";
import Navbar from "./components/Navbar";
import PageTransition from "./components/PageTransition";
import Landing from "./pages/Landing";
import PostItem from "./pages/PostItem";
import ItemDetail from "./pages/ItemDetail";
import MyItems from "./pages/MyItems";
import AdminDashboard from "./pages/AdminDashboard";
import Auth from "./pages/Auth";
import { AuthProvider, useAuth } from "./context/AuthContext";

function ProtectedAdminRoute({ children }) {
  const { user, isAdmin } = useAuth();

  if (!user) {
    return (
      <div className="max-w-md mx-auto px-6 py-28 text-center">
        <h2 className="font-serif-italic not-italic text-3xl mb-3">Admin access required.</h2>
        <p className="text-sm text-[#4a4a4a] mb-6">You must be logged in as an administrator to view this dashboard.</p>
        <Link
          to="/auth"
          className="px-6 py-3 border-[3px] border-ink bg-ink text-bone text-xs font-bold uppercase tracking-wide hover:bg-amber hover:text-ink transition-colors"
        >
          Sign In as Admin →
        </Link>
      </div>
    );
  }

  if (!isAdmin) {
    return (
      <div className="max-w-md mx-auto px-6 py-28 text-center">
        <div className="w-12 h-12 border-2 border-ink flex items-center justify-center mx-auto mb-5 bg-red-100 font-bold text-red-600">✕</div>
        <h2 className="font-serif-italic not-italic text-3xl mb-3">Access Denied</h2>
        <p className="text-sm text-[#4a4a4a] mb-6">Your account does not have administrator privileges.</p>
        <Link
          to="/"
          className="px-6 py-3 border-[3px] border-ink bg-ink text-bone text-xs font-bold uppercase tracking-wide hover:bg-amber hover:text-ink transition-colors"
        >
          ← Back to Manifest
        </Link>
      </div>
    );
  }

  return children;
}

// Redirects admin accounts away from student/public pages directly to /admin
function StudentOnlyRoute({ children }) {
  const { isAdmin } = useAuth();
  if (isAdmin) {
    return <Navigate to="/admin" replace />;
  }
  return children;
}

function AnimatedRoutes() {
  const location = useLocation();
  return (
    <AnimatePresence mode="wait">
      <Routes location={location} key={location.pathname}>
        <Route
          path="/"
          element={
            <PageTransition>
              <StudentOnlyRoute>
                <Landing />
              </StudentOnlyRoute>
            </PageTransition>
          }
        />
        <Route
          path="/post"
          element={
            <PageTransition>
              <StudentOnlyRoute>
                <PostItem />
              </StudentOnlyRoute>
            </PageTransition>
          }
        />
        <Route
          path="/items/:id"
          element={
            <PageTransition>
              <StudentOnlyRoute>
                <ItemDetail />
              </StudentOnlyRoute>
            </PageTransition>
          }
        />
        <Route
          path="/my-items"
          element={
            <PageTransition>
              <StudentOnlyRoute>
                <MyItems />
              </StudentOnlyRoute>
            </PageTransition>
          }
        />
        <Route
          path="/admin"
          element={
            <PageTransition>
              <ProtectedAdminRoute>
                <AdminDashboard />
              </ProtectedAdminRoute>
            </PageTransition>
          }
        />
        <Route path="/auth" element={<PageTransition><Auth /></PageTransition>} />
      </Routes>
    </AnimatePresence>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Navbar />
        <AnimatedRoutes />
      </BrowserRouter>
    </AuthProvider>
  );
}
