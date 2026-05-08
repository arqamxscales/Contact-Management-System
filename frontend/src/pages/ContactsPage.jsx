import React, { useEffect, useMemo, useState } from "react";
import { EmptyState } from "../components/EmptyState.jsx";
import { InfoCard } from "../components/InfoCard.jsx";
import { ContactModal } from "../components/ContactModal.jsx";
import { ConfirmDeleteModal } from "../components/ConfirmDeleteModal.jsx";
import { useAuth } from "../hooks/useAuth.js";
import {
  createContact,
  deleteContact,
  deleteContactsBatch,
  exportContactsCsv,
  listContactsPaged,
  searchContactsAdvanced,
  updateContact
} from "../api/contactApi.js";

export function ContactsPage() {
  // These filters are intentionally explicit so users can narrow by label quickly.
  const [searchTerm, setSearchTerm] = useState("");
  const [emailLabel, setEmailLabel] = useState("");
  const [phoneLabel, setPhoneLabel] = useState("");
  const { user } = useAuth();

  const [contacts, setContacts] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);

  const [selectedContact, setSelectedContact] = useState(null);
  const [selectedContactIds, setSelectedContactIds] = useState([]);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showUpdateModal, setShowUpdateModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const hasAdvancedFilters = useMemo(
    () => Boolean(emailLabel || phoneLabel),
    [emailLabel, phoneLabel]
  );

  async function fetchContacts() {
    setLoading(true);
    setError("");

    try {
      const params = {
        page: currentPage - 1,
        size: pageSize,
        search: searchTerm || undefined,
        emailLabel: emailLabel || undefined,
        phoneLabel: phoneLabel || undefined
      };

      // We keep these two branches separate so backend route changes are less risky.
      const response = hasAdvancedFilters
        ? await searchContactsAdvanced(params)
        : await listContactsPaged(params);

      const nextContacts = response?.content ?? [];
      setContacts(nextContacts);
      setTotalPages(Math.max(response?.totalPages ?? 1, 1));

      // Keep only selected ids that still exist after filtering/page changes.
      const idSet = new Set(nextContacts.map((c) => c.id));
      setSelectedContactIds((prev) => prev.filter((id) => idSet.has(id)));
    } catch (fetchError) {
      setError(fetchError.response?.data?.message ?? "Could not load contacts right now.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchContacts();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentPage, pageSize, searchTerm, emailLabel, phoneLabel]);

  const handleCreateContact = async (formData) => {
    try {
      await createContact(formData);
      setShowCreateModal(false);
      await fetchContacts();
    } catch (createError) {
      setError(createError.response?.data?.message ?? "Could not create contact.");
    }
  };

  const handleUpdateContact = async (formData) => {
    if (!selectedContact?.id) return;
    try {
      await updateContact(selectedContact.id, formData);
      setShowUpdateModal(false);
      setSelectedContact(null);
      await fetchContacts();
    } catch (updateError) {
      setError(updateError.response?.data?.message ?? "Could not update contact.");
    }
  };

  const handleDeleteContact = async (contactId) => {
    try {
      await deleteContact(contactId);
      setShowDeleteModal(false);
      setSelectedContact(null);
      await fetchContacts();
    } catch (deleteError) {
      setError(deleteError.response?.data?.message ?? "Could not delete contact.");
    }
  };

  const toggleSelectContact = (contactId, checked) => {
    setSelectedContactIds((prev) => {
      if (checked) {
        return Array.from(new Set([...prev, contactId]));
      }
      return prev.filter((id) => id !== contactId);
    });
  };

  const handleSelectAllOnPage = (checked) => {
    if (checked) {
      setSelectedContactIds((prev) => Array.from(new Set([...prev, ...contacts.map((c) => c.id)])));
      return;
    }

    const pageIds = new Set(contacts.map((c) => c.id));
    setSelectedContactIds((prev) => prev.filter((id) => !pageIds.has(id)));
  };

  const handleBatchDelete = async () => {
    if (selectedContactIds.length === 0) return;
    try {
      await deleteContactsBatch(selectedContactIds, user?.id);
      setSelectedContactIds([]);
      await fetchContacts();
    } catch (batchError) {
      setError(batchError.response?.data?.message ?? "Batch delete failed.");
    }
  };

  const handleBatchExport = async () => {
    if (selectedContactIds.length === 0) return;
    try {
      const blob = await exportContactsCsv(selectedContactIds, user?.id);
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = "contacts-export.csv";
      link.click();
      URL.revokeObjectURL(url);
    } catch (exportError) {
      setError(exportError.response?.data?.message ?? "Export failed.");
    }
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
      {error ? <p style={errorStyle}>{error}</p> : null}

      <div style={toolbarStyle}>
        <input
          type="text"
          placeholder="Search by name, email, phone..."
          value={searchTerm}
          onChange={(e) => {
            setSearchTerm(e.target.value);
            setCurrentPage(1);
          }}
          style={searchInputStyle}
        />

        <select
          value={emailLabel}
          onChange={(e) => {
            setEmailLabel(e.target.value);
            setCurrentPage(1);
          }}
          style={selectStyle}
          aria-label="Filter by email label"
        >
          <option value="">All email labels</option>
          <option value="work">Work email</option>
          <option value="personal">Personal email</option>
          <option value="other">Other email</option>
        </select>

        <select
          value={phoneLabel}
          onChange={(e) => {
            setPhoneLabel(e.target.value);
            setCurrentPage(1);
          }}
          style={selectStyle}
          aria-label="Filter by phone label"
        >
          <option value="">All phone labels</option>
          <option value="work">Work phone</option>
          <option value="home">Home phone</option>
          <option value="mobile">Mobile phone</option>
          <option value="other">Other phone</option>
        </select>
      </div>

      <div style={actionsBarStyle}>
        <button onClick={() => setShowCreateModal(true)} style={createButtonStyle}>
          + Create Contact
        </button>

        <button
          onClick={handleBatchDelete}
          disabled={selectedContactIds.length === 0}
          style={dangerButtonStyle}
        >
          Delete Selected ({selectedContactIds.length})
        </button>

        <button
          onClick={handleBatchExport}
          disabled={selectedContactIds.length === 0}
          style={secondaryButtonStyle}
        >
          Export Selected CSV
        </button>
      </div>

      {loading ? (
        <p style={mutedTextStyle}>Loading contacts...</p>
      ) : contacts.length > 0 ? (
        <>
          <label style={selectAllStyle}>
            <input
              type="checkbox"
              checked={contacts.length > 0 && contacts.every((c) => selectedContactIds.includes(c.id))}
              onChange={(e) => handleSelectAllOnPage(e.target.checked)}
            />
            Select all contacts on this page
          </label>

          <div style={gridStyle}>
            {contacts.map((contact) => (
              <article key={contact.id} style={contactCardStyle}>
                <label style={checkboxStyle}>
                  <input
                    type="checkbox"
                    checked={selectedContactIds.includes(contact.id)}
                    onChange={(e) => toggleSelectContact(contact.id, e.target.checked)}
                    aria-label={`select-contact-${contact.id}`}
                  />
                  Select
                </label>
                <strong>{contact.firstName} {contact.lastName}</strong>
                <p style={mutedTextStyle}>{contact.title}</p>
                <p style={{ marginBottom: "0.5rem" }}>
                  {contact.email ?? contact?.emails?.[0]?.address ?? "No email"}
                </p>
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
                <option value={5}>5 per page</option>
                <option value={10}>10 per page</option>
                <option value={20}>20 per page</option>
              </select>
            </div>
          )}
        </>
      ) : (
        <EmptyState
          title="No contacts found"
          message={
            searchTerm || emailLabel || phoneLabel
              ? "Try different filters or clear the current search."
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
  border: "1px solid #d1d5db",
  borderRadius: "0.75rem",
  padding: "0.8rem 0.9rem",
  fontSize: "1rem"
};

const toolbarStyle = {
  display: "grid",
  gridTemplateColumns: "2fr 1fr 1fr",
  gap: "0.75rem",
  marginBottom: "1rem"
};

const actionsBarStyle = {
  display: "flex",
  gap: "0.75rem",
  marginBottom: "1rem",
  flexWrap: "wrap"
};

const selectStyle = {
  border: "1px solid #d1d5db",
  borderRadius: "0.75rem",
  padding: "0.8rem 0.9rem",
  background: "white"
};

const createButtonStyle = {
  border: "none",
  borderRadius: "0.8rem",
  padding: "0.9rem 1rem",
  background: "#4f46e5",
  color: "white",
  cursor: "pointer",
};

const secondaryButtonStyle = {
  border: "1px solid #d1d5db",
  borderRadius: "0.8rem",
  padding: "0.9rem 1rem",
  background: "white",
  color: "#1f2937",
  cursor: "pointer"
};

const dangerButtonStyle = {
  border: "1px solid #dc2626",
  borderRadius: "0.8rem",
  padding: "0.9rem 1rem",
  background: "white",
  color: "#dc2626",
  cursor: "pointer"
};

const selectAllStyle = {
  display: "inline-flex",
  alignItems: "center",
  gap: "0.5rem",
  marginBottom: "0.75rem",
  color: "#374151"
};

const checkboxStyle = {
  display: "inline-flex",
  alignItems: "center",
  gap: "0.5rem",
  marginBottom: "0.5rem"
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

const errorStyle = {
  margin: "0 0 0.8rem 0",
  color: "#b91c1c"
};

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