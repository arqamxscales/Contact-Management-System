package com.cms.repository;

import com.cms.entity.RefreshToken;
import com.cms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for managing refresh tokens.
 * Handles CRUD operations and queries for token validation and revocation.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find a refresh token by its token string.
     * Used when user submits refresh token to get new access token.
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find all non-revoked, non-expired tokens for a user.
     * Used to check active sessions for the user.
     */
    java.util.List<RefreshToken> findByUserAndRevokedFalseAndExpiresAtAfter(User user, LocalDateTime now);

    /**
     * Count active refresh tokens for a user.
     * Can implement multi-device session limit if needed.
     */
    long countByUserAndRevokedFalseAndExpiresAtAfter(User user, LocalDateTime now);

    /**
     * Revoke all tokens for a user (logout all devices).
     * Used when user changes password or explicitly logs out everywhere.
     */
    void updateRevokedTrueByUser(User user);

    /**
     * Cleanup: Delete expired tokens older than specified date.
     * Run periodically via scheduled task to keep database clean.
     */
    void deleteByExpiresAtBefore(LocalDateTime cutoffDate);
}
