package com.cms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for user login requests.
 * Validates user credentials for authentication.
 */
public class UserLoginRequest {

    // User email address for login
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    // User password for authentication
    @NotBlank(message = "Password is required")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
