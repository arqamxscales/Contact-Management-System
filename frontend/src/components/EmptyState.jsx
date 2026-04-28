import React from "react";

export function EmptyState({ title, message }) {
  return (
    <div style={emptyStyle}>
      <h3>{title}</h3>
      <p style={{ margin: 0, color: "#6b7280" }}>{message}</p>
    </div>
  );
}

const emptyStyle = {
  border: "1px dashed #c7d2fe",
  background: "#eef2ff",
  borderRadius: "1rem",
  padding: "1.5rem",
  textAlign: "center"
};