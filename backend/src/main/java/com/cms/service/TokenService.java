package com.cms.service;

import com.cms.dto.RefreshTokenRequest;
import com.cms.dto.TokenResponse;
import com.cms.entity.RefreshToken;
import com.cms.entity.User;
import com.cms.exception.ResourceNotFoundException;
import com.cms.exception.UnauthorizedException;
import com.cms.repository.RefreshTokenRepository;
import com.cms.util.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for managing JWT token lifecycle - issuance, refresh, and revocation.
 * Handles both access tokens (short-lived, 15 min) and refresh tokens (long-lived, 7 days).
 * 
 * Token Flow:
 * 1. User logs in with credentials -> get access + refresh token
 * 2. Use access token for API requests (valid for 15 minutes)
 * 3. When access token expires, send refresh token -> get new access token
 * 4. When user logs out, revoke refresh token (invalidate all sessions)
 */
@Service
@Transactional
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserService userService;

    /**
     * Generate both access and refresh tokens for newly authenticated user.
     * Called after successful login with valid credentials.
     * 
     * @param user Authenticated user object
     * @return TokenResponse containing access token, refresh token, and expiration time
     */
    public TokenResponse generateTokens(User user) {
        // Generate short-lived access token (valid for 15 minutes)
        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
        
        // Create long-lived refresh token in database (valid for 7 days)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(7);
        
        RefreshToken refreshToken = new RefreshToken(
            user,
            jwtTokenProvider.generateRefreshToken(user.getId()),
            now,
            expiresAt
        );
        
        // Persist refresh token to database for validation on next use
        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        
        logger.info("Generated tokens for user: {}", user.getEmail());
        
        // Return both tokens to client - access token for immediate use, refresh for renewal
        return new TokenResponse(
            accessToken,
            savedToken.getToken(),
            900L  // 15 minutes in seconds
        );
    }

    /**
     * Refresh access token using valid refresh token.
     * Client sends expired/expiring access token + refresh token to get new access token.
     * This avoids requiring full re-login when access token expires.
     * 
     * @param request RefreshTokenRequest with both tokens
     * @return TokenResponse with new access token
     * @throws UnauthorizedException if refresh token is invalid, revoked, or expired
     */
    public TokenResponse refreshAccessToken(RefreshTokenRequest request) {
        // Retrieve refresh token from database
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
            .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        
        // Verify token is not revoked and hasn't expired
        if (!refreshToken.isValid()) {
            logger.warn("Attempted refresh with invalid/expired token for user: {}", 
                refreshToken.getUser().getEmail());
            throw new UnauthorizedException("Refresh token has expired or been revoked");
        }
        
        User user = refreshToken.getUser();
        
        // Generate new access token with same claims as original
        String newAccessToken = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
        
        logger.info("Refreshed access token for user: {}", user.getEmail());
        
        // Return new access token (refresh token remains valid)
        return new TokenResponse(
            newAccessToken,
            request.getRefreshToken(),
            900L  // 15 minutes in seconds
        );
    }

    /**
     * Revoke a single refresh token when user logs out from one device.
     * Client sends refresh token to invalidate that specific session.
     * 
     * @param token Refresh token to revoke
     * @throws ResourceNotFoundException if token doesn't exist
     */
    @Transactional
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
            .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));
        
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        
        logger.info("Revoked refresh token for user: {}", refreshToken.getUser().getEmail());
    }

    /**
     * Revoke ALL refresh tokens for a user (logout from all devices).
     * Called when user changes password or signs out from all devices.
     * 
     * @param userId User ID whose tokens should be revoked
     */
    @Transactional
    public void revokeAllRefreshTokens(Long userId) {
        User user = userService.getUserById(userId);
        
        // Mark all active tokens as revoked
        refreshTokenRepository.updateRevokedTrueByUser(user);
        
        logger.info("Revoked all refresh tokens for user: {}", user.getEmail());
    }

    /**
     * Cleanup job to remove expired tokens from database.
     * Should run periodically (daily) via @Scheduled task to keep DB clean.
     * Expired tokens are no longer usable and just consume storage space.
     */
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(8); // Keep 1 day buffer
        refreshTokenRepository.deleteByExpiresAtBefore(cutoffDate);
        logger.info("Cleaned up expired refresh tokens from database");
    }

    /**
     * Check if a user has any active sessions (valid, non-revoked refresh tokens).
     * Used to determine if user should be considered "logged in".
     * 
     * @param user User to check
     * @return true if user has at least one valid refresh token
     */
    public boolean hasActiveSessions(User user) {
        long activeTokenCount = refreshTokenRepository
            .countByUserAndRevokedFalseAndExpiresAtAfter(user, LocalDateTime.now());
        return activeTokenCount > 0;
    }
}
