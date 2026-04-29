import React from "react";
import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { ContactsPage } from "../pages/ContactsPage.jsx";

// Mock modals to avoid complexity in this test.
vi.mock("../components/ContactModal.jsx", () => ({
  ContactModal: ({ isOpen, onClose, contact, onSave }) => (
    isOpen ? (
      <div data-testid="contact-modal">
        {contact ? "Update Modal" : "Create Modal"}
        <button onClick={() => onSave({ firstName: "Test", lastName: "User", email: "test@example.com", title: "Mr", phone: "" })}>
          Save
        </button>
      </div>
    ) : null
  )
}));

vi.mock("../components/ConfirmDeleteModal.jsx", () => ({
  ConfirmDeleteModal: ({ isOpen, onClose, onConfirm }) => (
    isOpen ? (
      <div data-testid="delete-modal">
        <button onClick={() => onConfirm(1)}>Confirm Delete</button>
      </div>
    ) : null
  )
}));

describe("ContactsPage", () => {
  it("renders search input and create button", () => {
    render(
      <MemoryRouter>
        <ContactsPage />
      </MemoryRouter>
    );

    expect(screen.getByPlaceholderText(/Search by first or last name/)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Create Contact/ })).toBeInTheDocument();
  });

  it("displays paginated contacts", () => {
    render(
      <MemoryRouter>
        <ContactsPage />
      </MemoryRouter>
    );

    // First page should show only 2 contacts (default page size).
    expect(screen.getByText("Sam Lee")).toBeInTheDocument();
    expect(screen.getByText("Jane Smith")).toBeInTheDocument();
    expect(screen.queryByText("Bob Johnson")).not.toBeInTheDocument();
  });

  it("filters contacts by search term", async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <ContactsPage />
      </MemoryRouter>
    );

    const searchInput = screen.getByPlaceholderText(/Search by first or last name/);
    await user.type(searchInput, "Bob");

    expect(screen.getByText("Bob Johnson")).toBeInTheDocument();
    expect(screen.queryByText("Sam Lee")).not.toBeInTheDocument();
  });

  it("handles pagination navigation", async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <ContactsPage />
      </MemoryRouter>
    );

    // Click next page button.
    const nextButton = screen.getByRole("button", { name: "Next" });
    await user.click(nextButton);

    // Second page should show different contacts.
    expect(screen.queryByText("Sam Lee")).not.toBeInTheDocument();
    expect(screen.getByText("Bob Johnson")).toBeInTheDocument();
  });

  it("shows empty state when no search results", async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <ContactsPage />
      </MemoryRouter>
    );

    const searchInput = screen.getByPlaceholderText(/Search by first or last name/);
    await user.type(searchInput, "Nonexistent");

    expect(screen.getByText("No contacts found")).toBeInTheDocument();
  });

  it("opens create modal on create button click", async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <ContactsPage />
      </MemoryRouter>
    );

    await user.click(screen.getByRole("button", { name: /Create Contact/ }));

    expect(screen.getByTestId("contact-modal")).toBeInTheDocument();
    expect(screen.getByText("Create Modal")).toBeInTheDocument();
  });

  it("adds new contact when saved from modal", async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <ContactsPage />
      </MemoryRouter>
    );

    await user.click(screen.getByRole("button", { name: /Create Contact/ }));
    await user.click(screen.getByRole("button", { name: "Save" }));

    // New contact should appear in the list.
    expect(screen.getByText("Test User")).toBeInTheDocument();
  });

  it("changes page size", async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <ContactsPage />
      </MemoryRouter>
    );

    const pageSelect = screen.getByDisplayValue("2 per page");
    await user.selectOptions(pageSelect, "5");

    // All demo contacts should now fit on one page.
    expect(screen.getByText("Sam Lee")).toBeInTheDocument();
    expect(screen.getByText("Alice Brown")).toBeInTheDocument();
  });
});
