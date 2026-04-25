package com.cms.service;

import com.cms.dto.UserChangePasswordRequest;
import com.cms.dto.UserLoginRequest;
import com.cms.dto.UserRegistrationRequest;
import com.cms.dto.UserResponse;

/**
 * Service interface for user management operations.
 * Defines contracts for user registration, login, and profile management.
 */
public interface UserService {

    /**
     * Register a new user account.
     * Validates that email is not already in use and creates a new user.
     *
     * @param request user registration data
     * @return the newly created user information
     * @throws IllegalArgumentException if email is already registered
     */
    UserResponse register(UserRegistrationRequest request);

    /**
     * Authenticate a user with email and password.
     * Verifies credentials and returns user information if valid.
     *
     * @param request login credentials
     * @return the authenticated user information
     * @throws IllegalArgumentException if credentials are invalid
     */
    UserResponse login(UserLoginRequest request);

    /**
     * Retrieve user profile information.
     *
     * @param userId the user's ID
     * @return the user's profile information
     */
    UserResponse getUserProfile(Long userId);

    /**
     * Change password for a user.
     */
    void changePassword(Long userId, UserChangePasswordRequest request);
}
