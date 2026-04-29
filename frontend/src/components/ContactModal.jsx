import React, { useState, useEffect } from "react";
import { FormField } from "./FormField.jsx";

// This modal handles both create and update flows.
// We keep it simple so it stays reusable and testable.
export function ContactModal({ contact, isOpen, onClose, onSave }) {
  // Start with empty form or pre-populate for edit mode.
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    title: "",
    email: "",
    phone: ""
  });

  const [errors, setErrors] = useState({});

  // Populate form when contact is passed in (update mode).
  useEffect(() => {
    if (contact) {
      setForm(contact);
    } else {
      // Reset to empty form for create mode.
      setForm({
        firstName: "",
        lastName: "",
        title: "",
        email: "",
        phone: ""
      });
    }
    setErrors({});
  }, [contact, isOpen]);

  // Simple client-side validation before submit.
  const validate = () => {
    const newErrors = {};
    if (!form.firstName.trim()) newErrors.firstName = "First name is required";
    if (!form.lastName.trim()) newErrors.lastName = "Last name is required";
    if (!form.email.trim()) newErrors.email = "Email is required";
    if (form.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
      newErrors.email = "Email format is invalid";
    }
    return newErrors;
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    const newErrors = validate();
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }
    // Pass the form data back to the parent.
    onSave(form);
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div style={overlayStyle}>
      <div style={modalStyle}>
        <h2>{contact?.id ? "Update Contact" : "Create Contact"}</h2>
        <form onSubmit={handleSubmit} style={formStyle}>
          <FormField
            label="First Name"
            value={form.firstName}
            onChange={(e) => setForm({ ...form, firstName: e.target.value })}
          />
          {errors.firstName && <p style={errorStyle}>{errors.firstName}</p>}

          <FormField
            label="Last Name"
            value={form.lastName}
            onChange={(e) => setForm({ ...form, lastName: e.target.value })}
          />
          {errors.lastName && <p style={errorStyle}>{errors.lastName}</p>}

          <FormField
            label="Title (optional)"
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
          />

          <FormField
            label="Email"
            type="email"
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
          />
          {errors.email && <p style={errorStyle}>{errors.email}</p>}

          <FormField
            label="Phone (optional)"
            value={form.phone}
            onChange={(e) => setForm({ ...form, phone: e.target.value })}
          />

          <div style={buttonGroupStyle}>
            <button type="submit" style={primaryButtonStyle}>
              {contact?.id ? "Update" : "Create"}
            </button>
            <button
              type="button"
              onClick={onClose}
              style={secondaryButtonStyle}
            >
              Cancel
            </button>
          </div>
        </form>
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
  maxWidth: "500px",
  boxShadow: "0 20px 60px rgba(15, 23, 42, 0.2)"
};

const formStyle = {
  display: "grid",
  gap: "1rem"
};

const buttonGroupStyle = {
  display: "flex",
  gap: "1rem",
  marginTop: "1.5rem"
};

const primaryButtonStyle = {
  border: "none",
  borderRadius: "0.8rem",
  padding: "0.9rem 1rem",
  background: "#4f46e5",
  color: "white",
  cursor: "pointer",
  flex: 1
};

const secondaryButtonStyle = {
  border: "1px solid #d1d5db",
  borderRadius: "0.8rem",
  padding: "0.9rem 1rem",
  background: "white",
  color: "#1f2937",
  cursor: "pointer",
  flex: 1
};

const errorStyle = {
  margin: "0.25rem 0 0 0",
  color: "#b91c1c",
  fontSize: "0.875rem"
};
