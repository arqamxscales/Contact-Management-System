import React from "react";
import { useAuth } from "../hooks/useAuth.js";
import { InfoCard } from "../components/InfoCard.jsx";
import { Link } from "react-router-dom";

export function DashboardPage() {
  const { user } = useAuth();

  const initials = user?.fullName
    ? user.fullName
        .split(" ")
        .filter(Boolean)
        .slice(0, 2)
        .map((part) => part[0]?.toUpperCase())
        .join("")
    : "CM";

  return (
    <InfoCard title="Dashboard">
      <div style={heroStyle}>
        <div style={avatarStyle}>{initials}</div>
        <div>
          <p style={{ marginTop: 0, marginBottom: "0.35rem" }}>
            Welcome back{user?.fullName ? `, ${user.fullName}` : ""}.
          </p>
          <p style={{ margin: 0, color: "#6b7280" }}>
            This dashboard gives you a quick launch point for contacts, profile, and daily admin work.
          </p>
        </div>
      </div>

      <div style={quickActionsStyle}>
        <Link to="/contacts" style={quickActionLinkStyle}>Open Contacts</Link>
        <Link to="/profile" style={quickActionLinkStyle}>Open Profile</Link>
      </div>

      <ul style={listStyle}>
        <li>Review, search, and import contacts from the main workspace.</li>
        <li>Check your account details and change your password from profile.</li>
        <li>Keep the app moving without jumping through extra screens.</li>
      </ul>
    </InfoCard>
  );
}

const heroStyle = {
  display: "flex",
  gap: "1rem",
  alignItems: "center",
  marginBottom: "1.25rem"
};

const avatarStyle = {
  width: "3rem",
  height: "3rem",
  borderRadius: "999px",
  background: "#4f46e5",
  color: "white",
  display: "grid",
  placeItems: "center",
  fontWeight: 700,
  letterSpacing: "0.03em"
};

const quickActionsStyle = {
  display: "flex",
  gap: "0.75rem",
  flexWrap: "wrap",
  marginBottom: "1rem"
};

const quickActionLinkStyle = {
  display: "inline-flex",
  alignItems: "center",
  justifyContent: "center",
  border: "1px solid #d1d5db",
  borderRadius: "0.8rem",
  padding: "0.85rem 1rem",
  color: "#1f2937",
  textDecoration: "none",
  background: "white"
};

const listStyle = {
  marginBottom: 0,
  color: "#374151"
};