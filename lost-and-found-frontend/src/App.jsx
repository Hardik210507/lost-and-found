import { BrowserRouter, Routes, Route, useLocation } from "react-router-dom";
import { AnimatePresence } from "motion/react";
import Navbar from "./components/Navbar";
import PageTransition from "./components/PageTransition";
import Landing from "./pages/Landing";
import PostItem from "./pages/PostItem";
import ItemDetail from "./pages/ItemDetail";
import MyItems from "./pages/MyItems";
import AdminDashboard from "./pages/AdminDashboard";
import Auth from "./pages/Auth";

function AnimatedRoutes() {
  const location = useLocation();
  return (
    <AnimatePresence mode="wait">
      <Routes location={location} key={location.pathname}>
        <Route path="/" element={<PageTransition><Landing /></PageTransition>} />
        <Route path="/post" element={<PageTransition><PostItem /></PageTransition>} />
        <Route path="/items/:id" element={<PageTransition><ItemDetail /></PageTransition>} />
        <Route path="/my-items" element={<PageTransition><MyItems /></PageTransition>} />
        <Route path="/admin" element={<PageTransition><AdminDashboard /></PageTransition>} />
        <Route path="/auth" element={<PageTransition><Auth /></PageTransition>} />
      </Routes>
    </AnimatePresence>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <Navbar role="ADMIN" />
      <AnimatedRoutes />
    </BrowserRouter>
  );
}
