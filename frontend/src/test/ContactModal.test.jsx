import React from "react";
import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ContactModal } from "../components/ContactModal.jsx";
import { ConfirmDeleteModal } from "../components/ConfirmDeleteModal.jsx";

describe("ContactModal", () => {
  it("renders create form when contact is null", () => {
    render(
      <ContactModal
        contact={null}
        isOpen={true}
        onClose={vi.fn()}
        onSave={vi.fn()}
      />
    );

    expect(screen.getByText("Create Contact")).toBeInTheDocument();
    expect(screen.getByLabelText("First Name")).toHaveValue("");
  });

  it("renders update form and prepopulates data when contact is passed", () => {
    const contact = { id: 1, firstName: "John", lastName: "Doe", email: "john@example.com", title: "Mr", phone: "123456" };
    render(
      <ContactModal
        contact={contact}
        isOpen={true}
        onClose={vi.fn()}
        onSave={vi.fn()}
      />
    );

    expect(screen.getByText("Update Contact")).toBeInTheDocument();
    expect(screen.getByLabelText("First Name")).toHaveValue("John");
    expect(screen.getByLabelText("Last Name")).toHaveValue("Doe");
  });

  it("does not render when isOpen is false", () => {
    const { container } = render(
      <ContactModal
        contact={null}
        isOpen={false}
        onClose={vi.fn()}
        onSave={vi.fn()}
      />
    );

    expect(container.querySelector("form")).not.toBeInTheDocument();
  });

  it("calls onSave with form data on valid submit", async () => {
    const user = userEvent.setup();
    const onSave = vi.fn();

    render(
      <ContactModal
        contact={null}
        isOpen={true}
        onClose={vi.fn()}
        onSave={onSave}
      />
    );

    await user.type(screen.getByLabelText("First Name"), "Alice");
    await user.type(screen.getByLabelText("Last Name"), "Smith");
    await user.type(screen.getByLabelText("Email"), "alice@example.com");
    await user.click(screen.getByRole("button", { name: "Create" }));

    expect(onSave).toHaveBeenCalledWith(
      expect.objectContaining({
        firstName: "Alice",
        lastName: "Smith",
        email: "alice@example.com"
      })
    );
  });

  it("shows validation error when first name is empty", async () => {
    const user = userEvent.setup();
    render(
      <ContactModal
        contact={null}
        isOpen={true}
        onClose={vi.fn()}
        onSave={vi.fn()}
      />
    );

    await user.type(screen.getByLabelText("Last Name"), "Doe");
    await user.type(screen.getByLabelText("Email"), "test@example.com");
    await user.click(screen.getByRole("button", { name: "Create" }));

    expect(screen.getByText("First name is required")).toBeInTheDocument();
  });

  it("shows validation error for invalid email", async () => {
    const user = userEvent.setup();
    const onSave = vi.fn();
    render(
      <ContactModal
        contact={null}
        isOpen={true}
        onClose={vi.fn()}
        onSave={onSave}
      />
    );

    await user.type(screen.getByLabelText("First Name"), "John");
    await user.type(screen.getByLabelText("Last Name"), "Doe");
    await user.type(screen.getByLabelText("Email"), "invalid-email");
    await user.click(screen.getByRole("button", { name: "Create" }));

    // The invalid email should prevent saving.
    expect(onSave).not.toHaveBeenCalled();
  });
});

describe("ConfirmDeleteModal", () => {
  it("renders when isOpen is true and contact is provided", () => {
    const contact = { id: 1, firstName: "John", lastName: "Doe" };
    render(
      <ConfirmDeleteModal
        contact={contact}
        isOpen={true}
        onClose={vi.fn()}
        onConfirm={vi.fn()}
      />
    );

    expect(screen.getByText("Delete Contact?")).toBeInTheDocument();
    expect(screen.getByText(/John Doe/)).toBeInTheDocument();
  });

  it("does not render when isOpen is false", () => {
    const { container } = render(
      <ConfirmDeleteModal
        contact={{ id: 1, firstName: "John", lastName: "Doe" }}
        isOpen={false}
        onClose={vi.fn()}
        onConfirm={vi.fn()}
      />
    );

    expect(container.querySelector("h2")).not.toBeInTheDocument();
  });

  it("calls onConfirm with contact id on delete button click", async () => {
    const user = userEvent.setup();
    const contact = { id: 1, firstName: "John", lastName: "Doe" };
    const onConfirm = vi.fn();
    const onClose = vi.fn();

    render(
      <ConfirmDeleteModal
        contact={contact}
        isOpen={true}
        onClose={onClose}
        onConfirm={onConfirm}
      />
    );

    await user.click(screen.getByRole("button", { name: "Delete" }));

    expect(onConfirm).toHaveBeenCalledWith(1);
    expect(onClose).toHaveBeenCalled();
  });

  it("calls onClose on cancel button click", async () => {
    const user = userEvent.setup();
    const onClose = vi.fn();

    render(
      <ConfirmDeleteModal
        contact={{ id: 1, firstName: "John", lastName: "Doe" }}
        isOpen={true}
        onClose={onClose}
        onConfirm={vi.fn()}
      />
    );

    await user.click(screen.getByRole("button", { name: "Cancel" }));

    expect(onClose).toHaveBeenCalled();
  });
});
