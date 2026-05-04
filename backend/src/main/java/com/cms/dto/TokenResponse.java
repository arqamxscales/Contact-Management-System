package com.cms.dto;

/**
 * DTO for JWT token response after successful authentication.
 * Contains access token (short-lived) and refresh token (long-lived) for JWT-based auth flow.
 * Access token is used for API requests, refresh token is used to get new access tokens.
 */
public class TokenResponse {

    // Short-lived JWT token (typically 15 minutes) for API authentication
    private String accessToken;

    // Long-lived token (typically 7 days) used to refresh access token without re-login
    private String refreshToken;

    // Token type - always "Bearer" for JWT
    private String tokenType = "Bearer";

    // Expiration time of access token in seconds
    private Long expiresIn;

    // Constructor for building token response with all required fields
    public TokenResponse(String accessToken, String refreshToken, Long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
