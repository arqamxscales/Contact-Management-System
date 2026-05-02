package com.cms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a phone number associated with a contact.
 * Phone numbers can be labeled (work, home, personal, mobile, etc.) to support multiple numbers per contact.
 * This keeps the Contact entity clean while allowing flexible phone number management.
 */
@Entity
@Table(name = "phone_numbers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Phone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    // The label helps distinguish between work, home, personal, mobile, etc.
    @Column(nullable = false, length = 50)
    private String label; // e.g., "work", "home", "mobile", "personal"

    @Column(nullable = false, length = 20)
    private String number;

    // Track if this is the primary phone for the contact (optional convenience field).
    @Column(columnDefinition = "boolean default false")
    private Boolean isPrimary = false;

    public Phone(Contact contact, String label, String number) {
        this.contact = contact;
        this.label = label;
        this.number = number;
        this.isPrimary = false;
    }
}
