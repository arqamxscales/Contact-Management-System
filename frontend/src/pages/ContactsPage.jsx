import React from "react";
import { EmptyState } from "../components/EmptyState.jsx";
import { InfoCard } from "../components/InfoCard.jsx";

const demoContacts = [
  { id: 1, firstName: "Sam", lastName: "Lee", title: "Mr", email: "sam@example.com" },
  { id: 2, firstName: "Jane", lastName: "Smith", title: "Dr", email: "jane@example.com" }
];

export function ContactsPage() {
  const hasContacts = demoContacts.length > 0;

  return (
    <InfoCard title="Contacts">
      {hasContacts ? (
        <div style={gridStyle}>
          {demoContacts.map((contact) => (
            <article key={contact.id} style={contactCardStyle}>
              <strong>{contact.firstName} {contact.lastName}</strong>
              <p style={mutedTextStyle}>{contact.title}</p>
              <p style={{ marginBottom: 0 }}>{contact.email}</p>
            </article>
          ))}
        </div>
      ) : (
        <EmptyState title="No contacts yet" message="This is where the live list will appear once the API is wired in." />
      )}
    </InfoCard>
  );
}

const gridStyle = {
  display: "grid",
  gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
  gap: "1rem"
};

const contactCardStyle = {
  border: "1px solid #e5e7eb",
  borderRadius: "1rem",
  padding: "1rem",
  background: "#fafafa"
};

const mutedTextStyle = { color: "#6b7280", marginTop: 0 };