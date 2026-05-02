package com.cms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an email address associated with a contact.
 * Emails can be labeled (work, personal, etc.) to support multiple addresses per contact.
 * This design keeps the schema flexible while remaining queryable.
 */
@Entity
@Table(name = "email_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    // The label helps distinguish between work, personal, etc.
    @Column(nullable = false, length = 50)
    private String label; // e.g., "work", "personal", "home"

    @Column(nullable = false, length = 255)
    private String address;

    // Track if this is the primary email for the contact (optional convenience field).
    @Column(columnDefinition = "boolean default false")
    private Boolean isPrimary = false;

    public Email(Contact contact, String label, String address) {
        this.contact = contact;
        this.label = label;
        this.address = address;
        this.isPrimary = false;
    }
}
