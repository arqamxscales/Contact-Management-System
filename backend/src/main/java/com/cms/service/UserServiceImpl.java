package com.cms.service;

import com.cms.dto.UserChangePasswordRequest;
import com.cms.dto.UserLoginRequest;
import com.cms.dto.UserRegistrationRequest;
import com.cms.dto.UserResponse;
import com.cms.entity.User;
import com.cms.exception.ResourceNotFoundException;
import com.cms.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of UserService for managing user accounts.
 * Handles user registration, authentication, and profile retrieval.
 * Uses password encoding for security.
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse register(UserRegistrationRequest request) {
        // Check if email is already registered
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            // Email already in use - prevent duplicate registrations
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        // Create new user entity with provided information
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        // Hash password using password encoder for security
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());

        // Save user to database
        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getEmail());

        return toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse login(UserLoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        // Verify password matches the stored hash
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // Credentials don't match - prevent login
            log.warn("Failed login attempt for user: {}", request.getEmail());
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Login successful
        log.info("User logged in: {}", user.getEmail());
        return toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserProfile(Long userId) {
        // Retrieve user by ID, throw exception if not found
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        return toResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, UserChangePasswordRequest request) {
        // Find user first.
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        // Verify current password before updating.
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getEmail());
    }

    /**
     * Helper method to convert User entity to UserResponse DTO.
     * Excludes password hash from response for security.
     */
    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
