import React from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth.js";
import { logoutAllSessions } from "../api/authApi.js";

export function AppShell({ children }) {
  const { isAuthenticated, logout, user } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    // We try to revoke server-side sessions first, then clear the local session either way.
    if (user?.id) {
      try {
        await logoutAllSessions({ userId: user.id });
      } catch (logoutError) {
        // If the server is unavailable, we still let the user sign out locally.
        void logoutError;
      }
    }

    logout();
    navigate("/login");
  };

  return (
    <div>
      <header style={headerStyle}>
        <Link to="/dashboard" style={brandStyle}>Contact Manager</Link>
        <nav style={navStyle}>
          <NavLink to="/dashboard" style={navLinkStyle}>Dashboard</NavLink>
          <NavLink to="/contacts" style={navLinkStyle}>Contacts</NavLink>
          <NavLink to="/profile" style={navLinkStyle}>Profile</NavLink>
          {!isAuthenticated ? (
            <NavLink to="/login" style={navLinkStyle}>Login</NavLink>
          ) : (
            <button onClick={handleLogout} style={buttonStyle}>Logout {user?.fullName ? `(${user.fullName})` : ""}</button>
          )}
        </nav>
      </header>
      <main style={mainStyle}>{children}</main>
    </div>
  );
}

const headerStyle = {
  display: "flex",
  justifyContent: "space-between",
  alignItems: "center",
  padding: "1rem 1.5rem",
  background: "white",
  borderBottom: "1px solid #e5e7eb"
};

const brandStyle = {
  fontWeight: 700,
  color: "#4f46e5"
};

const navStyle = {
  display: "flex",
  gap: "1rem",
  alignItems: "center"
};

const navLinkStyle = ({ isActive }) => ({
  color: isActive ? "#4f46e5" : "#374151",
  fontWeight: isActive ? 700 : 500
});

const buttonStyle = {
  border: "none",
  borderRadius: "999px",
  padding: "0.6rem 1rem",
  background: "#4f46e5",
  color: "white",
  cursor: "pointer"
};

const mainStyle = {
  maxWidth: "1100px",
  margin: "0 auto",
  padding: "2rem 1rem"
};