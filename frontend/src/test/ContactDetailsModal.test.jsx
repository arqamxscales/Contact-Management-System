import React from "react";
import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ContactDetailsModal } from "../components/ContactDetailsModal.jsx";

describe("ContactDetailsModal", () => {
  it("shows the selected contact profile and closes cleanly", async () => {
    const user = userEvent.setup();
    const onClose = vi.fn();

    render(
      <ContactDetailsModal
        isOpen={true}
        onClose={onClose}
        contact={{
          id: 7,
          userId: 3,
          firstName: "Maya",
          lastName: "Chen",
          title: "Lead",
          email: "maya@example.com",
          phone: "5551234567",
          address: "1 Main Street",
          createdAt: "2026-05-11T09:30:00Z"
        }}
      />
    );

    expect(screen.getByRole("dialog", { name: /contact details/i })).toBeInTheDocument();
    expect(screen.getByText("Maya Chen")).toBeInTheDocument();
    expect(screen.getByText("maya@example.com")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: /close/i }));
    expect(onClose).toHaveBeenCalledTimes(1);
  });
});