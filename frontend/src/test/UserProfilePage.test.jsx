import React from "react";
import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { UserProfilePage } from "../pages/UserProfilePage.jsx";

vi.mock("../api/authApi.js", () => ({
  changePassword: vi.fn().mockResolvedValue({ success: true })
}));

const mockLogout = vi.fn();
vi.mock("../hooks/useAuth.js", () => ({
  useAuth: () => ({
    user: { id: 1, fullName: "John Doe", email: "john@example.com" },
    logout: mockLogout
  })
}));

describe("UserProfilePage", () => {
  it("renders user information", () => {
    render(<UserProfilePage />);

    expect(screen.getByText(/John Doe/)).toBeInTheDocument();
    expect(screen.getByText(/john@example.com/)).toBeInTheDocument();
  });

  it("shows change password button initially", () => {
    render(<UserProfilePage />);

    expect(screen.getByRole("button", { name: "Change Password" })).toBeInTheDocument();
  });

  it("shows password form when change password button is clicked", async () => {
    const user = userEvent.setup();
    render(<UserProfilePage />);

    await user.click(screen.getByRole("button", { name: "Change Password" }));

    expect(screen.getByLabelText("Current Password")).toBeInTheDocument();
    expect(screen.getByLabelText("New Password")).toBeInTheDocument();
    expect(screen.getByLabelText("Confirm New Password")).toBeInTheDocument();
  });

  it("validates password fields on submit", async () => {
    const user = userEvent.setup();
    render(<UserProfilePage />);

    await user.click(screen.getByRole("button", { name: "Change Password" }));
    await user.click(screen.getByRole("button", { name: /Update Password/ }));

    expect(screen.getByText("Current password is required")).toBeInTheDocument();
  });

  it("shows error when passwords do not match", async () => {
    const user = userEvent.setup();
    render(<UserProfilePage />);

    await user.click(screen.getByRole("button", { name: "Change Password" }));
    await user.type(screen.getByLabelText("Current Password"), "oldpass");
    await user.type(screen.getByLabelText("New Password"), "newpass");
    await user.type(screen.getByLabelText("Confirm New Password"), "different");
    await user.click(screen.getByRole("button", { name: /Update Password/ }));

    expect(screen.getByText("Passwords do not match")).toBeInTheDocument();
  });

  it("renders logout button", () => {
    render(<UserProfilePage />);

    expect(screen.getByRole("button", { name: "Logout" })).toBeInTheDocument();
  });
});
