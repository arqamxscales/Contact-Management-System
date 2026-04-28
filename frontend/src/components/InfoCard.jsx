import React from "react";

export function InfoCard({ title, children, actions }) {
  return (
    <section style={cardStyle}>
      <div style={topRowStyle}>
        <h2 style={{ margin: 0 }}>{title}</h2>
        {actions}
      </div>
      <div>{children}</div>
    </section>
  );
}

const cardStyle = {
  background: "white",
  border: "1px solid #e5e7eb",
  borderRadius: "1rem",
  padding: "1.25rem",
  boxShadow: "0 10px 30px rgba(15, 23, 42, 0.04)"
};

const topRowStyle = {
  display: "flex",
  justifyContent: "space-between",
  alignItems: "center",
  marginBottom: "1rem"
};