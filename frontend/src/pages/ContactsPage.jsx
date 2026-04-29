import React, { useState, useMemo } from "react";
import { EmptyState } from "../components/EmptyState.jsx";
import { InfoCard } from "../components/InfoCard.jsx";
import { ContactModal } from "../components/ContactModal.jsx";
import { ConfirmDeleteModal } from "../components/ConfirmDeleteModal.jsx";

// Demo contacts. When the backend is ready, replace this with API calls.
const demoContacts = [
  { id: 1, firstName: "Sam", lastName: "Lee", title: "Mr", email: "sam@example.com" },
  { id: 2, firstName: "Jane", lastName: "Smith", title: "Dr", email: "jane@example.com" },
  { id: 3, firstName: "Bob", lastName: "Johnson", title: "Mr", email: "bob@example.com" },
  { id: 4, firstName: "Alice", lastName: "Brown", title: "Ms", email: "alice@example.com" }
];

export function ContactsPage() {
  // State for search, modals, pagination, and demo contacts management.
  const [searchTerm, setSearchTerm] = useState("");
  const [contacts, setContacts] = useState(demoContacts);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(2);
  const [selectedContact, setSelectedContact] = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showUpdateModal, setShowUpdateModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  // Filter contacts by search term (case-insensitive).
  const filteredContacts = useMemo(() => {
    if (!searchTerm) return contacts;
    const term = searchTerm.toLowerCase();
    return contacts.filter(
      (c) =>
        c.firstName.toLowerCase().includes(term) ||
        c.lastName.toLowerCase().includes(term)
    );
  }, [contacts, searchTerm]);

  // Calculate pagination.
  const totalPages = Math.ceil(filteredContacts.length / pageSize);
  const paginatedContacts = filteredContacts.slice(
    (currentPage - 1) * pageSize,
    currentPage * pageSize
  );

  const handleCreateContact = (formData) => {
    const newContact = {
      ...formData,
      id: Math.max(...contacts.map((c) => c.id), 0) + 1
    };
    const next = [...contacts, newContact];
    setContacts(next);
    // After creating a new contact, jump to the last page so the user sees it immediately.
    setCurrentPage(Math.ceil(next.length / pageSize));
    setShowCreateModal(false);
  };

  const handleUpdateContact = (formData) => {
    setContacts(
      contacts.map((c) => (c.id === selectedContact.id ? { ...formData, id: c.id } : c))
    );
    setShowUpdateModal(false);
    setSelectedContact(null);
  };

  const handleDeleteContact = (contactId) => {
    setContacts(contacts.filter((c) => c.id !== contactId));
    setShowDeleteModal(false);
  };

  const openUpdateModal = (contact) => {
    setSelectedContact(contact);
    setShowUpdateModal(true);
  };

  const openDeleteModal = (contact) => {
    setSelectedContact(contact);
    setShowDeleteModal(true);
  };

  return (
    <InfoCard title="Contacts">
      {/* Search bar */}
      <input
        type="text"
        placeholder="Search by first or last name..."
        value={searchTerm}
        onChange={(e) => {
          setSearchTerm(e.target.value);
          setCurrentPage(1); // Reset to first page on new search.
        }}
        style={searchInputStyle}
      />

      {/* Create button */}
      <button
        onClick={() => setShowCreateModal(true)}
        style={createButtonStyle}
      >
        + Create Contact
      </button>

      {/* Contacts list or empty state */}
      {paginatedContacts.length > 0 ? (
        <>
          <div style={gridStyle}>
            {paginatedContacts.map((contact) => (
              <article key={contact.id} style={contactCardStyle}>
                <strong>{contact.firstName} {contact.lastName}</strong>
                <p style={mutedTextStyle}>{contact.title}</p>
                <p style={{ marginBottom: "0.5rem" }}>{contact.email}</p>
                <div style={cardActionsStyle}>
                  <button
                    onClick={() => openUpdateModal(contact)}
                    style={editButtonStyle}
                  >
                    Edit
                  </button>
                  <button
                    onClick={() => openDeleteModal(contact)}
                    style={deleteButtonStyle}
                  >
                    Delete
                  </button>
                </div>
              </article>
            ))}
          </div>

          {/* Pagination controls */}
          {totalPages > 1 && (
            <div style={paginationStyle}>
              <button
                onClick={() => setCurrentPage(Math.max(1, currentPage - 1))}
                disabled={currentPage === 1}
                style={paginationButtonStyle}
              >
                Previous
              </button>
              <span>
                Page {currentPage} of {totalPages}
              </span>
              <button
                onClick={() => setCurrentPage(Math.min(totalPages, currentPage + 1))}
                disabled={currentPage === totalPages}
                style={paginationButtonStyle}
              >
                Next
              </button>
              <select
                value={pageSize}
                onChange={(e) => {
                  setPageSize(Number(e.target.value));
                  setCurrentPage(1);
                }}
                style={pageSizeSelectStyle}
              >
                <option value={2}>2 per page</option>
                <option value={5}>5 per page</option>
                <option value={10}>10 per page</option>
              </select>
            </div>
          )}
        </>
      ) : (
        <EmptyState
          title="No contacts found"
          message={
            searchTerm
              ? "Try a different search term."
              : "Create your first contact to get started."
          }
        />
      )}

      {/* Modals */}
      <ContactModal
        contact={null}
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onSave={handleCreateContact}
      />

      <ContactModal
        contact={selectedContact}
        isOpen={showUpdateModal}
        onClose={() => {
          setShowUpdateModal(false);
          setSelectedContact(null);
        }}
        onSave={handleUpdateContact}
      />

      <ConfirmDeleteModal
        contact={selectedContact}
        isOpen={showDeleteModal}
        onClose={() => {
          setShowDeleteModal(false);
          setSelectedContact(null);
        }}
        onConfirm={handleDeleteContact}
      />
    </InfoCard>
  );
}

const searchInputStyle = {
  width: "100%",
  border: "1px solid #d1d5db",
  borderRadius: "0.75rem",
  padding: "0.8rem 0.9rem",
  marginBottom: "1rem",
  fontSize: "1rem"
};

const createButtonStyle = {
  border: "none",
  borderRadius: "0.8rem",
  padding: "0.9rem 1rem",
  background: "#4f46e5",
  color: "white",
  cursor: "pointer",
  marginBottom: "1.5rem"
};

const gridStyle = {
  display: "grid",
  gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
  gap: "1rem",
  marginBottom: "1.5rem"
};

const contactCardStyle = {
  border: "1px solid #e5e7eb",
  borderRadius: "1rem",
  padding: "1rem",
  background: "#fafafa"
};

const mutedTextStyle = { color: "#6b7280", marginTop: 0 };

const cardActionsStyle = {
  display: "flex",
  gap: "0.5rem",
  marginTop: "0.75rem"
};

const editButtonStyle = {
  border: "1px solid #4f46e5",
  borderRadius: "0.5rem",
  padding: "0.5rem 0.75rem",
  background: "white",
  color: "#4f46e5",
  cursor: "pointer",
  fontSize: "0.875rem",
  flex: 1
};

const deleteButtonStyle = {
  border: "1px solid #dc2626",
  borderRadius: "0.5rem",
  padding: "0.5rem 0.75rem",
  background: "white",
  color: "#dc2626",
  cursor: "pointer",
  fontSize: "0.875rem",
  flex: 1
};

const paginationStyle = {
  display: "flex",
  justifyContent: "center",
  alignItems: "center",
  gap: "1rem",
  marginTop: "1.5rem",
  padding: "1rem",
  background: "#f9fafb",
  borderRadius: "1rem"
};

const paginationButtonStyle = {
  border: "1px solid #d1d5db",
  borderRadius: "0.5rem",
  padding: "0.6rem 1rem",
  background: "white",
  cursor: "pointer"
};

const pageSizeSelectStyle = {
  border: "1px solid #d1d5db",
  borderRadius: "0.5rem",
  padding: "0.6rem",
  background: "white",
  cursor: "pointer"
};