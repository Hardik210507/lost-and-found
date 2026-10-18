import { BrowserRouter, Routes, Route, useLocation, Navigate } from "react-router-dom";
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

// Redirect unauthenticated users to /auth
function RequireAuth({ children }) {
  const { isAuthenticated } = useAuth();
  if (!isAuthenticated) {
    return <Navigate to="/auth" replace />;
  }
  return children;
}

// Redirect authenticated admins away from student pages → /admin
// Redirect authenticated non-admins away from /auth → /
function StudentOnlyRoute({ children }) {
  const { isAuthenticated, isAdmin } = useAuth();
  if (!isAuthenticated) return <Navigate to="/auth" replace />;
  if (isAdmin) return <Navigate to="/admin" replace />;
  return children;
}

// Redirect non-admins away from /admin
function AdminOnlyRoute({ children }) {
  const { isAuthenticated, isAdmin } = useAuth();
  if (!isAuthenticated) return <Navigate to="/auth" replace />;
  if (!isAdmin) return <Navigate to="/" replace />;
  return children;
}

// Redirect already-logged-in users away from /auth to the right place
function GuestOnlyRoute({ children }) {
  const { isAuthenticated, isAdmin } = useAuth();
  if (isAuthenticated) {
    return isAdmin ? <Navigate to="/admin" replace /> : <Navigate to="/" replace />;
  }
  return children;
}

function AnimatedRoutes() {
  const location = useLocation();
  return (
    <AnimatePresence mode="wait">
      <Routes location={location} key={location.pathname}>
        {/* Auth page — only accessible when NOT logged in */}
        <Route
          path="/auth"
          element={
            <PageTransition>
              <GuestOnlyRoute>
                <Auth />
              </GuestOnlyRoute>
            </PageTransition>
          }
        />

        {/* Student / user routes — require login, redirect admins to /admin */}
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

        {/* Admin route — require login + ADMIN role */}
        <Route
          path="/admin"
          element={
            <PageTransition>
              <AdminOnlyRoute>
                <AdminDashboard />
              </AdminOnlyRoute>
            </PageTransition>
          }
        />

        {/* Catch-all — redirect to /auth so unauthenticated users land on login */}
        <Route path="*" element={<Navigate to="/auth" replace />} />
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
