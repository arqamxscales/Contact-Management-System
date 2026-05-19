import React from "react";

// We keep the detail view in a modal so the contacts list stays easy to scan.
export function ContactDetailsModal({ contact, isOpen, onClose }) {
  if (!isOpen || !contact) {
    return null;
  }

  return (
    <div style={overlayStyle} role="dialog" aria-modal="true" aria-label="Contact details">
      <div style={modalStyle}>
        <h2 style={{ marginTop: 0 }}>Contact Details</h2>

        <div style={detailGridStyle}>
          <DetailRow label="Name" value={`${contact.firstName ?? ""} ${contact.lastName ?? ""}`.trim() || "N/A"} />
          <DetailRow label="Title" value={contact.title || "N/A"} />
          <DetailRow label="Email" value={contact.email || "N/A"} />
          <DetailRow label="Phone" value={contact.phone || "N/A"} />
          <DetailRow label="Address" value={contact.address || "N/A"} />
          <DetailRow label="Contact ID" value={contact.id ?? "N/A"} />
          <DetailRow label="Owner User ID" value={contact.userId ?? "N/A"} />
          <DetailRow
            label="Created At"
            value={contact.createdAt ? new Date(contact.createdAt).toLocaleString() : "N/A"}
          />
        </div>

        <div style={buttonRowStyle}>
          <button type="button" onClick={onClose} style={closeButtonStyle}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
}

function DetailRow({ label, value }) {
  return (
    <div style={rowStyle}>
      <dt style={labelStyle}>{label}</dt>
      <dd style={valueStyle}>{value}</dd>
    </div>
  );
}

const overlayStyle = {
  position: "fixed",
  inset: 0,
  background: "rgba(15, 23, 42, 0.55)",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  zIndex: 1100,
  padding: "1rem"
};

const modalStyle = {
  background: "white",
  borderRadius: "1rem",
  padding: "1.5rem",
  width: "100%",
  maxWidth: "520px",
  boxShadow: "0 20px 60px rgba(15, 23, 42, 0.2)"
};

const detailGridStyle = {
  display: "grid",
  gap: "0.85rem",
  marginTop: "1rem"
};

const rowStyle = {
  display: "grid",
  gap: "0.25rem",
  padding: "0.75rem 0.9rem",
  borderRadius: "0.85rem",
  background: "#f9fafb",
  border: "1px solid #e5e7eb"
};

const labelStyle = {
  fontSize: "0.8rem",
  color: "#6b7280",
  textTransform: "uppercase",
  letterSpacing: "0.06em"
};

const valueStyle = {
  margin: 0,
  color: "#111827",
  wordBreak: "break-word"
};

const buttonRowStyle = {
  display: "flex",
  justifyContent: "flex-end",
  marginTop: "1.25rem"
};

const closeButtonStyle = {
  border: "1px solid #d1d5db",
  borderRadius: "0.8rem",
  padding: "0.8rem 1rem",
  background: "white",
  color: "#1f2937",
  cursor: "pointer"
};