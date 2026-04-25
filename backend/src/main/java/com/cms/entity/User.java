package com.cms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User entity class representing a user account in the system.
 * Each user can have multiple contacts and manage their own contact information.
 * Users authenticate using email and password.
 */
@Entity
@Table(name = "users")
public class User {

    // Unique identifier for the user, auto-generated
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    // Email address used for login - must be unique
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // Optional phone number - must be unique if provided
    @Column(length = 25, unique = true)
    private String phone;

    // Hashed password stored securely in the database
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    // Timestamp when the user account was created
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Bidirectional relationship - one user has many contacts
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Contact> contacts = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }
}