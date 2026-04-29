import React from "react";

// A minimal confirmation modal. We keep it focused on delete confirmation.
export function ConfirmDeleteModal({ contact, isOpen, onClose, onConfirm }) {
  if (!isOpen || !contact) return null;

  const handleConfirm = () => {
    onConfirm(contact.id);
    onClose();
  };

  return (
    <div style={overlayStyle}>
      <div style={modalStyle}>
        <h2>Delete Contact?</h2>
        <p style={messageStyle}>
          Are you sure you want to delete {contact.firstName} {contact.lastName}?
          This action cannot be undone.
        </p>
        <div style={buttonGroupStyle}>
          <button
            onClick={handleConfirm}
            style={deleteButtonStyle}
          >
            Delete
          </button>
          <button
            onClick={onClose}
            style={cancelButtonStyle}
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
}

const overlayStyle = {
  position: "fixed",
  top: 0,
  left: 0,
  right: 0,
  bottom: 0,
  background: "rgba(0, 0, 0, 0.5)",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  zIndex: 1000
};

const modalStyle = {
  background: "white",
  borderRadius: "1rem",
  padding: "2rem",
  width: "90%",
  maxWidth: "450px",
  boxShadow: "0 20px 60px rgba(15, 23, 42, 0.2)"
};

const messageStyle = {
  margin: "1rem 0",
  color: "#6b7280",
  lineHeight: "1.5"
};

const buttonGroupStyle = {
  display: "flex",
  gap: "1rem",
  marginTop: "1.5rem"
};

const deleteButtonStyle = {
  border: "none",
  borderRadius: "0.8rem",
  padding: "0.9rem 1rem",
  background: "#dc2626",
  color: "white",
  cursor: "pointer",
  flex: 1
};

const cancelButtonStyle = {
  border: "1px solid #d1d5db",
  borderRadius: "0.8rem",
  padding: "0.9rem 1rem",
  background: "white",
  color: "#1f2937",
  cursor: "pointer",
  flex: 1
};
