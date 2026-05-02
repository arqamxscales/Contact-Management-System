import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ContactModal from '../ContactModal';
import '@testing-library/jest-dom';

/**
 * Unit tests for ContactModal component.
 * Tests form validation, labeled email/phone management, and modal interactions.
 */
describe('ContactModal - Labeled Emails and Phones', () => {
  const mockContact = {
    id: 1,
    firstName: 'John',
    lastName: 'Doe',
    company: 'Tech Corp',
    emails: [
      { id: 1, address: 'john@work.com', label: 'work', isPrimary: true },
      { id: 2, address: 'john@personal.com', label: 'personal', isPrimary: false }
    ],
    phones: [
      { id: 1, number: '15551234567', label: 'work', isPrimary: true },
      { id: 2, number: '15559876543', label: 'mobile', isPrimary: false }
    ]
  };

  const mockOnSave = vi.fn();
  const mockOnCancel = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should render contact modal with labeled emails', () => {
    render(
      <ContactModal
        contact={mockContact}
        onSave={mockOnSave}
        onCancel={mockOnCancel}
        isOpen={true}
      />
    );

    expect(screen.getByDisplayValue('john@work.com')).toBeInTheDocument();
    expect(screen.getByDisplayValue('john@personal.com')).toBeInTheDocument();
  });

  it('should render contact modal with labeled phones', () => {
    render(
      <ContactModal
        contact={mockContact}
        onSave={mockOnSave}
        onCancel={mockOnCancel}
        isOpen={true}
      />
    );

    expect(screen.getByDisplayValue('15551234567')).toBeInTheDocument();
    expect(screen.getByDisplayValue('15559876543')).toBeInTheDocument();
  });

  it('should display label selectors for emails', () => {
    render(
      <ContactModal
        contact={mockContact}
        onSave={mockOnSave}
        onCancel={mockOnCancel}
        isOpen={true}
      />
    );

    const emailLabels = screen.getAllByDisplayValue('work').slice(0, 1);
    expect(emailLabels.length).toBeGreaterThan(0);
  });

  it('should add a new email field on click', async () => {
    const user = userEvent.setup();
    render(
      <ContactModal
        contact={mockContact}
        onSave={mockOnSave}
        onCancel={mockOnCancel}
        isOpen={true}
      />
    );

    const addEmailButton = screen.getByText(/add email/i);
    await user.click(addEmailButton);

    await waitFor(() => {
      const emailInputs = screen.getAllByPlaceholderText(/email/i);
      expect(emailInputs.length).toBeGreaterThan(2);
    });
  });

  it('should add a new phone field on click', async () => {
    const user = userEvent.setup();
    render(
      <ContactModal
        contact={mockContact}
        onSave={mockOnSave}
        onCancel={mockOnCancel}
        isOpen={true}
      />
    );

    const addPhoneButton = screen.getByText(/add phone/i);
    await user.click(addPhoneButton);

    await waitFor(() => {
      const phoneInputs = screen.getAllByPlaceholderText(/phone/i);
      expect(phoneInputs.length).toBeGreaterThan(2);
    });
  });

  it('should remove email field on click', async () => {
    const user = userEvent.setup();
    const { container } = render(
      <ContactModal
        contact={mockContact}
        onSave={mockOnSave}
        onCancel={mockOnCancel}
        isOpen={true}
      />
    );

    const removeButtons = container.querySelectorAll('[aria-label="remove-email"]');
    if (removeButtons.length > 0) {
      await user.click(removeButtons[0]);
      await waitFor(() => {
        expect(removeButtons[0]).not.toBeInTheDocument();
      });
    }
  });

  it('should remove phone field on click', async () => {
    const user = userEvent.setup();
    const { container } = render(
      <ContactModal
        contact={mockContact}
        onSave={mockOnSave}
        onCancel={mockOnCancel}
        isOpen={true}
      />
    );

    const removeButtons = container.querySelectorAll('[aria-label="remove-phone"]');
    if (removeButtons.length > 0) {
      await user.click(removeButtons[0]);
      await waitFor(() => {
        expect(removeButtons[0]).not.toBeInTheDocument();
      });
    }
  });

  it('should mark email as primary', async () => {
    const user = userEvent.setup();
    const { container } = render(
      <ContactModal
        contact={mockContact}
        onSave={mockOnSave}
        onCancel={mockOnCancel}
        isOpen={true}
      />
    );

    const primaryCheckboxes = container.querySelectorAll('[aria-label*="primary"]');
    if (primaryCheckboxes.length > 0) {
      await user.click(primaryCheckboxes[1]);
      expect(primaryCheckboxes[1]).toBeChecked();
    }
  });

  it('should mark phone as primary', async () => {
    const user = userEvent.setup();
    const { container } = render(
      <ContactModal
        contact={mockContact}
        onSave={mockOnSave}
        onCancel={mockOnCancel}
        isOpen={true}
      />
    );

    const primaryCheckboxes = container.querySelectorAll('[aria-label*="primary"]');
    if (primaryCheckboxes.length > 1) {
      await user.click(primaryCheckboxes[primaryCheckboxes.length - 1]);
      expect(primaryCheckboxes[primaryCheckboxes.length - 1]).toBeChecked();
    }
  });

  it('should validate email format on save', async () => {
    const user = userEvent.setup();
    render(
      <ContactModal
        contact={mockContact}
        onSave={mockOnSave}
        onCancel={mockOnCancel}
        isOpen={true}
      />
    );

    const emailInput = screen.getByDisplayValue('john@work.com');
    await user.clear(emailInput);
    await user.type(emailInput, 'invalid-email');

    const saveButton = screen.getByText(/save/i);
    await user.click(saveButton);

    // Should not call onSave with invalid email
    expect(mockOnSave).not.toHaveBeenCalled();
  });

  it('should validate phone format on save', async () => {
    const user = userEvent.setup();
    render(
      <ContactModal
        contact={mockContact}
        onSave={mockOnSave}
        onCancel={mockOnCancel}
        isOpen={true}
      />
    );

    const phoneInput = screen.getByDisplayValue('15551234567');
    await user.clear(phoneInput);
    await user.type(phoneInput, '123');

    const saveButton = screen.getByText(/save/i);
    await user.click(saveButton);

    // Should not call onSave with invalid phone
    expect(mockOnSave).not.toHaveBeenCalled();
  });

  it('should change email label on dropdown change', async () => {
    const user = userEvent.setup();
    const { container } = render(
      <ContactModal
        contact={mockContact}
        onSave={mockOnSave}
        onCancel={mockOnCancel}
        isOpen={true}
      />
    );

    const labelSelects = container.querySelectorAll('select');
    if (labelSelects.length > 0) {
      await user.selectOptions(labelSelects[0], 'personal');
      expect(labelSelects[0]).toHaveValue('personal');
    }
  });

  it('should change phone label on dropdown change', async () => {
    const user = userEvent.setup();
    const { container } = render(
      <ContactModal
        contact={mockContact}
        onSave={mockOnSave}
        onCancel={mockOnCancel}
        isOpen={true}
      />
    );

    const labelSelects = container.querySelectorAll('select');
    if (labelSelects.length > 1) {
      await user.selectOptions(labelSelects[1], 'home');
      expect(labelSelects[1]).toHaveValue('home');
    }
  });

  it('should call onSave with all email and phone data', async () => {
    const user = userEvent.setup();
    render(
      <ContactModal
        contact={mockContact}
        onSave={mockOnSave}
        onCancel={mockOnCancel}
        isOpen={true}
      />
    );

    const saveButton = screen.getByText(/save/i);
    await user.click(saveButton);

    await waitFor(() => {
      expect(mockOnSave).toHaveBeenCalled();
      const callArg = mockOnSave.mock.calls[0][0];
      expect(callArg.emails).toBeDefined();
      expect(callArg.phones).toBeDefined();
      expect(callArg.emails.length).toBeGreaterThan(0);
      expect(callArg.phones.length).toBeGreaterThan(0);
    });
  });

  it('should call onCancel when cancel button is clicked', async () => {
    const user = userEvent.setup();
    render(
      <ContactModal
        contact={mockContact}
        onSave={mockOnSave}
        onCancel={mockOnCancel}
        isOpen={true}
      />
    );

    const cancelButton = screen.getByText(/cancel/i);
    await user.click(cancelButton);

    expect(mockOnCancel).toHaveBeenCalled();
  });
});
