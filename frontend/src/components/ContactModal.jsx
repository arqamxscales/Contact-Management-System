import React, { useState, useEffect } from "react";
import { FormField } from "./FormField.jsx";

// Email label options - work, personal, or other
const EMAIL_LABELS = ["work", "personal", "other"];

// Phone label options - work, home, mobile, or other
const PHONE_LABELS = ["work", "home", "mobile", "other"];

// This modal handles both create and update flows with support for multiple labeled emails/phones.
// Users can add/remove email and phone entries with custom labels and primary flags.
export function ContactModal({ contact, isOpen, onClose, onSave }) {
  // Start with empty form or pre-populate for edit mode.
  // Emails and phones are now arrays of { address/number, label, isPrimary }
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    title: "",
    emails: [{ address: "", label: "work", isPrimary: true }],
    phones: [{ number: "", label: "work", isPrimary: true }]
  });

  const [errors, setErrors] = useState({});

  // Populate form when contact is passed in (update mode).
  // Convert legacy email/phone fields to new array format if needed.
  useEffect(() => {
    if (contact) {
      const emails = contact.emails?.length > 0 
        ? contact.emails 
        : (contact.email ? [{ address: contact.email, label: "work", isPrimary: true }] : [{ address: "", label: "work", isPrimary: true }]);
      
      const phones = contact.phones?.length > 0 
        ? contact.phones 
        : (contact.phone ? [{ number: contact.phone, label: "work", isPrimary: true }] : [{ number: "", label: "work", isPrimary: true }]);

      setForm({
        firstName: contact.firstName || "",
        lastName: contact.lastName || "",
        title: contact.title || "",
        emails,
        phones
      });
    } else {
      // Reset to empty form for create mode with default email/phone entries
      setForm({
        firstName: "",
        lastName: "",
        title: "",
        emails: [{ address: "", label: "work", isPrimary: true }],
        phones: [{ number: "", label: "work", isPrimary: true }]
      });
    }
    setErrors({});
  }, [contact, isOpen]);

  // Validate emails array - at least one primary email required with valid format
  const validateEmails = () => {
    const emailErrors = [];
    let hasPrimary = false;
    
    form.emails.forEach((email, idx) => {
      if (email.address.trim()) {
        // Email format validation
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.address)) {
          emailErrors[idx] = "Invalid email format";
        }
        if (email.isPrimary) hasPrimary = true;
      }
    });
    
    if (!hasPrimary && form.emails.some(e => e.address.trim())) {
      emailErrors.primary = "Mark one email as primary";
    }
    
    return emailErrors;
  };

  // Validate phones array - at most one primary phone
  const validatePhones = () => {
    const phoneErrors = [];
    let primaryCount = 0;
    
    form.phones.forEach((phone, idx) => {
      if (phone.number.trim()) {
        // Basic phone validation - should be at least 10 digits, allow +, -, (, )
        if (!/^[\d+\-()]{10,}$/.test(phone.number.replace(/\s/g, ''))) {
          phoneErrors[idx] = "Invalid phone format";
        }
        if (phone.isPrimary) primaryCount++;
      }
    });
    
    if (primaryCount > 1) {
      phoneErrors.primary = "Only one phone can be primary";
    }
    
    return phoneErrors;
  };

  // Simple client-side validation before submit.
  const validate = () => {
    const newErrors = {};
    if (!form.firstName.trim()) newErrors.firstName = "First name is required";
    if (!form.lastName.trim()) newErrors.lastName = "Last name is required";
    
    const emailErrors = validateEmails();
    const phoneErrors = validatePhones();
    
    if (Object.keys(emailErrors).length > 0) newErrors.emails = emailErrors;
    if (Object.keys(phoneErrors).length > 0) newErrors.phones = phoneErrors;
    
    return newErrors;
  };

  // Handle adding a new email field
  const addEmail = () => {
    setForm({
      ...form,
      emails: [...form.emails, { address: "", label: "work", isPrimary: false }]
    });
  };

  // Handle removing an email field
  const removeEmail = (idx) => {
    const updated = form.emails.filter((_, i) => i !== idx);
    // Ensure at least one email entry
    if (updated.length === 0) {
      updated.push({ address: "", label: "work", isPrimary: true });
    }
    setForm({ ...form, emails: updated });
  };

  // Handle adding a new phone field
  const addPhone = () => {
    setForm({
      ...form,
      phones: [...form.phones, { number: "", label: "mobile", isPrimary: false }]
    });
  };

  // Handle removing a phone field
  const removePhone = (idx) => {
    const updated = form.phones.filter((_, i) => i !== idx);
    // Ensure at least one phone entry
    if (updated.length === 0) {
      updated.push({ number: "", label: "mobile", isPrimary: false });
    }
    setForm({ ...form, phones: updated });
  };

  // Handle email field changes
  const handleEmailChange = (idx, field, value) => {
    const updated = [...form.emails];
    updated[idx] = { ...updated[idx], [field]: value };
    
    // If setting this email as primary, unset others
    if (field === "isPrimary" && value) {
      updated.forEach((e, i) => {
        if (i !== idx) e.isPrimary = false;
      });
    }
    
    setForm({ ...form, emails: updated });
  };

  // Handle phone field changes
  const handlePhoneChange = (idx, field, value) => {
    const updated = [...form.phones];
    updated[idx] = { ...updated[idx], [field]: value };
    
    // If setting this phone as primary, unset others
    if (field === "isPrimary" && value) {
      updated.forEach((p, i) => {
        if (i !== idx) p.isPrimary = false;
      });
    }
    
    setForm({ ...form, phones: updated });
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

          {/* Multiple labeled emails section */}
          <div style={sectionStyle}>
            <h3 style={sectionTitleStyle}>Emails</h3>
            {form.emails.map((email, idx) => (
              <div key={idx} style={fieldGroupStyle}>
                <div style={fieldRowStyle}>
                  <select
                    value={email.label}
                    onChange={(e) => handleEmailChange(idx, "label", e.target.value)}
                    style={selectStyle}
                    aria-label={`email-label-${idx}`}
                  >
                    {EMAIL_LABELS.map(label => (
                      <option key={label} value={label}>{label}</option>
                    ))}
                  </select>
                  <input
                    type="email"
                    placeholder="Email address"
                    value={email.address}
                    onChange={(e) => handleEmailChange(idx, "address", e.target.value)}
                    style={inputStyle}
                  />
                </div>
                <div style={checkboxRowStyle}>
                  <label style={checkboxLabelStyle}>
                    <input
                      type="checkbox"
                      checked={email.isPrimary}
                      onChange={(e) => handleEmailChange(idx, "isPrimary", e.target.checked)}
                      aria-label={`primary-email-${idx}`}
                    />
                    Primary
                  </label>
                  {form.emails.length > 1 && (
                    <button
                      type="button"
                      onClick={() => removeEmail(idx)}
                      style={removeButtonStyle}
                      aria-label="remove-email"
                    >
                      Remove
                    </button>
                  )}
                </div>
                {errors.emails?.[idx] && <p style={errorStyle}>{errors.emails[idx]}</p>}
              </div>
            ))}
            {errors.emails?.primary && <p style={errorStyle}>{errors.emails.primary}</p>}
            <button
              type="button"
              onClick={addEmail}
              style={addButtonStyle}
            >
              + Add Email
            </button>
          </div>

          {/* Multiple labeled phones section */}
          <div style={sectionStyle}>
            <h3 style={sectionTitleStyle}>Phones</h3>
            {form.phones.map((phone, idx) => (
              <div key={idx} style={fieldGroupStyle}>
                <div style={fieldRowStyle}>
                  <select
                    value={phone.label}
                    onChange={(e) => handlePhoneChange(idx, "label", e.target.value)}
                    style={selectStyle}
                    aria-label={`phone-label-${idx}`}
                  >
                    {PHONE_LABELS.map(label => (
                      <option key={label} value={label}>{label}</option>
                    ))}
                  </select>
                  <input
                    type="tel"
                    placeholder="Phone number"
                    value={phone.number}
                    onChange={(e) => handlePhoneChange(idx, "number", e.target.value)}
                    style={inputStyle}
                  />
                </div>
                <div style={checkboxRowStyle}>
                  <label style={checkboxLabelStyle}>
                    <input
                      type="checkbox"
                      checked={phone.isPrimary}
                      onChange={(e) => handlePhoneChange(idx, "isPrimary", e.target.checked)}
                      aria-label={`primary-phone-${idx}`}
                    />
                    Primary
                  </label>
                  {form.phones.length > 1 && (
                    <button
                      type="button"
                      onClick={() => removePhone(idx)}
                      style={removeButtonStyle}
                      aria-label="remove-phone"
                    >
                      Remove
                    </button>
                  )}
                </div>
                {errors.phones?.[idx] && <p style={errorStyle}>{errors.phones[idx]}</p>}
              </div>
            ))}
            {errors.phones?.primary && <p style={errorStyle}>{errors.phones.primary}</p>}
            <button
              type="button"
              onClick={addPhone}
              style={addButtonStyle}
            >
              + Add Phone
            </button>
          </div>

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

const sectionStyle = {
  marginTop: "1.5rem",
  paddingTop: "1rem",
  borderTop: "1px solid #e5e7eb"
};

const sectionTitleStyle = {
  margin: "0 0 1rem 0",
  fontSize: "0.875rem",
  fontWeight: "600",
  color: "#374151",
  textTransform: "uppercase",
  letterSpacing: "0.05em"
};

const fieldGroupStyle = {
  marginBottom: "1rem",
  padding: "0.75rem",
  backgroundColor: "#f9fafb",
  borderRadius: "0.5rem"
};

const fieldRowStyle = {
  display: "grid",
  gridTemplateColumns: "auto 1fr",
  gap: "0.75rem",
  marginBottom: "0.5rem"
};

const selectStyle = {
  padding: "0.5rem 0.75rem",
  border: "1px solid #d1d5db",
  borderRadius: "0.4rem",
  fontSize: "0.875rem",
  backgroundColor: "white",
  color: "#1f2937",
  cursor: "pointer"
};

const inputStyle = {
  padding: "0.5rem 0.75rem",
  border: "1px solid #d1d5db",
  borderRadius: "0.4rem",
  fontSize: "0.875rem",
  fontFamily: "inherit"
};

const checkboxRowStyle = {
  display: "flex",
  alignItems: "center",
  gap: "1rem"
};

const checkboxLabelStyle = {
  display: "flex",
  alignItems: "center",
  gap: "0.5rem",
  fontSize: "0.875rem",
  color: "#4b5563",
  cursor: "pointer"
};

const addButtonStyle = {
  marginTop: "0.5rem",
  padding: "0.5rem 0.75rem",
  border: "1px dashed #4f46e5",
  borderRadius: "0.4rem",
  background: "white",
  color: "#4f46e5",
  cursor: "pointer",
  fontSize: "0.875rem",
  fontWeight: "500"
};

const removeButtonStyle = {
  padding: "0.4rem 0.75rem",
  border: "1px solid #ef4444",
  borderRadius: "0.4rem",
  background: "white",
  color: "#ef4444",
  cursor: "pointer",
  fontSize: "0.75rem"
};
