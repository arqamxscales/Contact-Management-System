package com.cms.controller;

import com.cms.dto.LogoutAllRequest;
import com.cms.dto.RefreshTokenRequest;
import com.cms.dto.TokenResponse;
import com.cms.dto.UserChangePasswordRequest;
import com.cms.dto.UserLoginRequest;
import com.cms.dto.UserRegistrationRequest;
import com.cms.dto.UserResponse;
import com.cms.service.TokenService;
import com.cms.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user authentication and profile management.
 * Handles user registration, login, and profile retrieval.
 * All endpoints are prefixed with /api/auth
 */
@RestController
@RequestMapping("/api/auth")
public class UserController {

    // Logger for tracking authentication operations
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final TokenService tokenService;

    public UserController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    /**
     * Register a new user account.
     * POST /api/auth/register
     *
     * @param request user registration data with email, password, and name
     * @return the newly created user information with 201 Created status
     * @throws IllegalArgumentException if email is already registered
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("New user registration attempt: {}", request.getEmail());
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticate a user with email and password.
     * POST /api/auth/login
     *
     * @param request login credentials (email and password)
     * @return the authenticated user information
     * @throws IllegalArgumentException if credentials are invalid
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody UserLoginRequest request) {
        log.info("Login attempt: {}", request.getEmail());
        UserResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token using a valid refresh token.
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refreshing access token");
        return ResponseEntity.ok(tokenService.refreshAccessToken(request));
    }

    /**
     * Revoke every active session for the given user.
     * POST /api/auth/logout-all
     */
    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@Valid @RequestBody LogoutAllRequest request) {
        log.info("Revoking all sessions for user id={}", request.getUserId());
        tokenService.revokeAllRefreshTokens(request.getUserId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieve the authenticated user's profile information.
     * GET /api/auth/profile/{userId}
     *
     * @param userId the ID of the user whose profile is being requested
     * @return the user's profile information
     */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserResponse> getProfile(@PathVariable Long userId) {
        UserResponse response = userService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Change user password.
     * POST /api/auth/profile/{userId}/change-password
     */
    @PostMapping("/profile/{userId}/change-password")
    public ResponseEntity<Void> changePassword(
        @PathVariable Long userId,
        @Valid @RequestBody UserChangePasswordRequest request
    ) {
        userService.changePassword(userId, request);
        return ResponseEntity.noContent().build();
    }
}
