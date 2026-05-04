package com.cms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * RefreshToken entity for managing long-lived JWT refresh tokens.
 * Stores issued refresh tokens to enable token refresh operations without re-login.
 * Tokens are invalidated after expiration or on user logout for security.
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_user_token", columnList = "user_id, token")
})
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reference to user who owns this refresh token
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // The actual refresh token JWT string
    @Column(nullable = false, length = 2048)
    private String token;

    // Timestamp when this token was issued
    @Column(name = "issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    // Timestamp when this token will expire and can no longer be used
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // Soft-delete: token is revoked when user logs out or issues new token
    @Column(name = "revoked", nullable = false)
    private Boolean revoked = false;

    // Default constructor for Hibernate
    public RefreshToken() {
    }

    // Constructor for creating a new refresh token with expiration
    public RefreshToken(User user, String token, LocalDateTime issuedAt, LocalDateTime expiresAt) {
        this.user = user;
        this.token = token;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.revoked = false;
    }

    // Check if token is still valid (not expired and not revoked)
    public boolean isValid() {
        return !revoked && expiresAt.isAfter(LocalDateTime.now());
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getRevoked() {
        return revoked;
    }

    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
    }
}
