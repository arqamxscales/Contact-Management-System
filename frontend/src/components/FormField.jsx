import React from "react";

export function FormField({ label, ...props }) {
  return (
    <label style={wrapperStyle}>
      <span style={labelStyle}>{label}</span>
      <input {...props} style={inputStyle} />
    </label>
  );
}

const wrapperStyle = {
  display: "grid",
  gap: "0.45rem"
};

const labelStyle = {
  fontSize: "0.95rem",
  fontWeight: 600
};

const inputStyle = {
  border: "1px solid #d1d5db",
  borderRadius: "0.75rem",
  padding: "0.8rem 0.9rem",
  background: "white"
};