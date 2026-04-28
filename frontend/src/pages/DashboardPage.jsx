import React from "react";
import { useAuth } from "../hooks/useAuth.js";
import { InfoCard } from "../components/InfoCard.jsx";

export function DashboardPage() {
  const { user } = useAuth();

  return (
    <InfoCard title="Dashboard">
      <p style={{ marginTop: 0 }}>Welcome back{user?.fullName ? `, ${user.fullName}` : ""}.</p>
      <ul style={{ marginBottom: 0 }}>
        <li>Use the contacts view to browse records.</li>
        <li>Use this shell as the base for future frontend tasks.</li>
      </ul>
    </InfoCard>
  );
}