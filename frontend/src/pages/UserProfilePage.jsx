import React, { useState, useEffect } from "react";
import { useAuth } from "../hooks/useAuth.js";
import { changePassword } from "../api/authApi.js";
import { FormField } from "../components/FormField.jsx";
import { InfoCard } from "../components/InfoCard.jsx";

// User profile page shows current user info and provides password change.
// We keep the change-password form inline for simplicity here, but it could be a modal too.
export function UserProfilePage() {
  const { user, logout } = useAuth();
  const [showPasswordForm, setShowPasswordForm] = useState(false);
  const [passwordForm, setPasswordForm] = useState({
    oldPassword: "",
    newPassword: "",
    confirmPassword: ""
  });
  const [passwordError, setPasswordError] = useState("");
  const [passwordSuccess, setPasswordSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  const handlePasswordSubmit = async (event) => {
    event.preventDefault();
    setPasswordError("");
    setPasswordSuccess("");

    // Basic validation before sending to backend.
    if (!passwordForm.oldPassword) {
      setPasswordError("Current password is required");
      return;
    }
    if (!passwordForm.newPassword) {
      setPasswordError("New password is required");
      return;
    }
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setPasswordError("Passwords do not match");
      return;
    }

    setLoading(true);
    try {
      await changePassword(user?.id, {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      });
      setPasswordSuccess("Password changed successfully");
      setPasswordForm({ oldPassword: "", newPassword: "", confirmPassword: "" });
      setShowPasswordForm(false);
    } catch (error) {
      setPasswordError(error.response?.data?.message ?? "Failed to change password");
    } finally {
      setLoading(false);
    }
  };

  if (!user) {
    return (
      <InfoCard title="User Profile">
        <p>Not logged in. Please log in first.</p>
      </InfoCard>
    );
  }

  return (
    <InfoCard title="User Profile">
      <div style={profileContentStyle}>
        <div style={userInfoStyle}>
          <h3>Account Information</h3>
          <p><strong>Name:</strong> {user.fullName || "N/A"}</p>
          <p><strong>Email:</strong> {user.email || "N/A"}</p>
          <p><strong>User ID:</strong> {user.id || "N/A"}</p>
        </div>

        <div style={actionButtonsStyle}>
          {!showPasswordForm ? (
            <button
              onClick={() => setShowPasswordForm(true)}
              style={changePasswordButtonStyle}
            >
              Change Password
            </button>
          ) : null}
          <button
            onClick={() => {
              logout();
              window.location.href = "/login";
            }}
            style={logoutButtonStyle}
          >
            Logout
          </button>
        </div>

        {showPasswordForm && (
          <div style={passwordFormContainerStyle}>
            <h4>Change Password</h4>
            <form onSubmit={handlePasswordSubmit} style={formStyle}>
              <FormField
                label="Current Password"
                type="password"
                value={passwordForm.oldPassword}
                onChange={(e) =>
                  setPasswordForm({ ...passwordForm, oldPassword: e.target.value })
                }
              />
              <FormField
                label="New Password"
                type="password"
                value={passwordForm.newPassword}
                onChange={(e) =>
                  setPasswordForm({ ...passwordForm, newPassword: e.target.value })
                }
              />
              <FormField
                label="Confirm New Password"
                type="password"
                value={passwordForm.confirmPassword}
                onChange={(e) =>
                  setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })
                }
              />
              {passwordError && <p style={errorStyle}>{passwordError}</p>}
              {passwordSuccess && <p style={successStyle}>{passwordSuccess}</p>}
              <div style={passwordButtonGroupStyle}>
                <button type="submit" style={submitButtonStyle} disabled={loading}>
                  {loading ? "Updating..." : "Update Password"}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setShowPasswordForm(false);
                    setPasswordForm({ oldPassword: "", newPassword: "", confirmPassword: "" });
                    setPasswordError("");
                    setPasswordSuccess("");
                  }}
                  style={cancelButtonStyle}
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        )}
      </div>
    </InfoCard>
  );
}

const profileContentStyle = {
  display: "grid",
  gap: "2rem"
};

const userInfoStyle = {
  background: "#f9fafb",
  borderRadius: "1rem",
  padding: "1.5rem",
  border: "1px solid #e5e7eb"
};

const actionButtonsStyle = {
  display: "flex",
  gap: "1rem"
};

const changePasswordButtonStyle = {
  border: "none",
  borderRadius: "0.8rem",
  padding: "0.9rem 1rem",
  background: "#4f46e5",
  color: "white",
  cursor: "pointer"
};

const logoutButtonStyle = {
  border: "1px solid #d1d5db",
  borderRadius: "0.8rem",
  padding: "0.9rem 1rem",
  background: "white",
  color: "#1f2937",
  cursor: "pointer"
};

const passwordFormContainerStyle = {
  background: "#fef3c7",
  borderRadius: "1rem",
  padding: "1.5rem",
  border: "1px solid #fbbf24"
};

const formStyle = {
  display: "grid",
  gap: "1rem"
};

const passwordButtonGroupStyle = {
  display: "flex",
  gap: "1rem"
};

const submitButtonStyle = {
  border: "none",
  borderRadius: "0.8rem",
  padding: "0.9rem 1rem",
  background: "#4f46e5",
  color: "white",
  cursor: "pointer"
};

const cancelButtonStyle = {
  border: "1px solid #d1d5db",
  borderRadius: "0.8rem",
  padding: "0.9rem 1rem",
  background: "white",
  color: "#1f2937",
  cursor: "pointer"
};

const errorStyle = {
  color: "#b91c1c",
  margin: "0.5rem 0 0 0"
};

const successStyle = {
  color: "#15803d",
  margin: "0.5rem 0 0 0"
};
