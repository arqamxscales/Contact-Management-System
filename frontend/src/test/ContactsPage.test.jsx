import React from "react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { ContactsPage } from "../pages/ContactsPage.jsx";

vi.mock("../api/contactApi.js", () => ({
  listContactsPaged: vi.fn(),
  searchContactsAdvanced: vi.fn(),
  createContact: vi.fn(),
  updateContact: vi.fn(),
  deleteContact: vi.fn(),
  deleteContactsBatch: vi.fn(),
  exportContactsCsv: vi.fn()
}));

vi.mock("../components/ContactModal.jsx", () => ({
  ContactModal: ({ isOpen, contact, onSave }) =>
    isOpen ? (
      <div data-testid="contact-modal">
        {contact ? "Update Modal" : "Create Modal"}
        <button onClick={() => onSave({ firstName: "Test", lastName: "User", email: "test@example.com" })}>
          Save
        </button>
      </div>
    ) : null
}));

vi.mock("../components/ConfirmDeleteModal.jsx", () => ({
  ConfirmDeleteModal: ({ isOpen, onConfirm }) =>
    isOpen ? (
      <div data-testid="delete-modal">
        <button onClick={() => onConfirm(1)}>Confirm Delete</button>
      </div>
    ) : null
}));

import {
  createContact,
  deleteContactsBatch,
  exportContactsCsv,
  listContactsPaged,
  searchContactsAdvanced
} from "../api/contactApi.js";

const pageResponse = {
  content: [
    { id: 1, firstName: "Sam", lastName: "Lee", title: "Mr", email: "sam@example.com" },
    { id: 2, firstName: "Jane", lastName: "Smith", title: "Dr", email: "jane@example.com" }
  ],
  totalPages: 2
};

describe("ContactsPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    listContactsPaged.mockResolvedValue(pageResponse);
    searchContactsAdvanced.mockResolvedValue(pageResponse);
    createContact.mockResolvedValue({});
    deleteContactsBatch.mockResolvedValue({ successCount: 1 });
    exportContactsCsv.mockResolvedValue(new Blob(["id,name\\n1,Sam"]));
  });

  it("loads contacts from API on render", async () => {
    render(
      <MemoryRouter>
        <ContactsPage />
      </MemoryRouter>
    );

    await waitFor(() => expect(listContactsPaged).toHaveBeenCalled());
    expect(screen.getByText("Sam Lee")).toBeInTheDocument();
  });

  it("uses advanced helper when label filter is set", async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <ContactsPage />
      </MemoryRouter>
    );

    await user.selectOptions(screen.getByLabelText(/Filter by email label/i), "work");

    await waitFor(() => expect(searchContactsAdvanced).toHaveBeenCalled());
  });

  it("creates contact through modal", async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <ContactsPage />
      </MemoryRouter>
    );

    await user.click(screen.getByRole("button", { name: /Create Contact/i }));
    await user.click(screen.getByRole("button", { name: "Save" }));

    await waitFor(() => expect(createContact).toHaveBeenCalled());
  });

  it("runs batch delete with selected IDs", async () => {
    const user = userEvent.setup();

    render(
      <MemoryRouter>
        <ContactsPage />
      </MemoryRouter>
    );

    await waitFor(() => expect(screen.getByLabelText("select-contact-1")).toBeInTheDocument());

    await user.click(screen.getByLabelText("select-contact-1"));
    await user.click(screen.getByRole("button", { name: /Delete Selected/i }));

    await waitFor(() => expect(deleteContactsBatch).toHaveBeenCalledWith([1]));
  });

  it("runs export with selected IDs", async () => {
    const user = userEvent.setup();
    const createObjectURLSpy = vi.spyOn(URL, "createObjectURL").mockReturnValue("blob:url");
    const revokeObjectURLSpy = vi.spyOn(URL, "revokeObjectURL").mockImplementation(() => {});

    render(
      <MemoryRouter>
        <ContactsPage />
      </MemoryRouter>
    );

    await waitFor(() => expect(screen.getByLabelText("select-contact-2")).toBeInTheDocument());

    await user.click(screen.getByLabelText("select-contact-2"));
    await user.click(screen.getByRole("button", { name: /Export Selected CSV/i }));

    await waitFor(() => expect(exportContactsCsv).toHaveBeenCalledWith([2]));

    createObjectURLSpy.mockRestore();
    revokeObjectURLSpy.mockRestore();
  });
});
