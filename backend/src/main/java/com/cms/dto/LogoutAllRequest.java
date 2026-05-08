package com.cms.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO used when logging out all sessions for a user.
 * The UI sends the current user id so the backend can revoke every refresh token.
 */
public class LogoutAllRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}