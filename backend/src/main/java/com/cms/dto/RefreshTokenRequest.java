package com.cms.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for JWT refresh token requests.
 * Client sends their expired access token and refresh token to get a new access token
 * without requiring full login credentials again.
 */
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    @NotBlank(message = "Access token is required")
    private String accessToken;

    // Default constructor for Jackson deserialization
    public RefreshTokenRequest() {
    }

    public RefreshTokenRequest(String refreshToken, String accessToken) {
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
