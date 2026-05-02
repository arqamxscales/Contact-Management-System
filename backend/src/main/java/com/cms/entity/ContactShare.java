package com.cms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * ContactShare entity tracks who has access to which contacts.
 * Supports basic contact-level sharing and permissions for collaboration.
 * Owner (createdBy) can share their contacts with other users.
 */
@Entity
@Table(name = "contact_shares")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The contact being shared
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    // The user who owns/created the contact
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // The user with whom the contact is shared
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_with_id", nullable = false)
    private User sharedWith;

    // Permission level: "read" (view only) or "write" (edit)
    @Column(nullable = false, length = 20)
    private String permission = "read";

    // When the share was created
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // When the share was last modified
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ContactShare(Contact contact, User owner, User sharedWith, String permission) {
        this.contact = contact;
        this.owner = owner;
        this.sharedWith = sharedWith;
        this.permission = permission;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
