package com.cms.service;

import com.cms.dto.UserChangePasswordRequest;
import com.cms.dto.UserLoginRequest;
import com.cms.dto.UserRegistrationRequest;
import com.cms.dto.UserResponse;
import com.cms.entity.User;
import com.cms.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for UserService implementation.
 * Tests user registration, login, and profile retrieval functionality.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserRegistrationRequest registrationRequest;
    private UserLoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPhone("5551234567");
        testUser.setPasswordHash("$2a$10$hashedPassword123");
        testUser.setCreatedAt(LocalDateTime.now());

        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setFullName("John Doe");
        registrationRequest.setEmail("john@example.com");
        registrationRequest.setPassword("password123");
        registrationRequest.setPhone("5551234567");

        loginRequest = new UserLoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("password123");
    }

    /**
     * Test successful user registration.
     */
    @Test
    void registerCreatesNewUserSuccessfully() {
        given(userRepository.findByEmail(registrationRequest.getEmail())).willReturn(Optional.empty());
        given(passwordEncoder.encode(anyString())).willReturn("$2a$10$hashedPassword123");
        given(userRepository.save(any(User.class))).willReturn(testUser);

        UserResponse result = userService.register(registrationRequest);

        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
        assertEquals("John Doe", result.getFullName());
        verify(userRepository).save(any(User.class));
    }

    /**
     * Test that registration fails when email is already registered.
     */
    @Test
    void registerThrowsExceptionWhenEmailAlreadyExists() {
        given(userRepository.findByEmail(registrationRequest.getEmail())).willReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, () -> userService.register(registrationRequest));
    }

    /**
     * Test successful user login with correct credentials.
     */
    @Test
    void loginReturnsUserWhenCredentialsAreValid() {
        given(userRepository.findByEmail(loginRequest.getEmail())).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash())).willReturn(true);

        UserResponse result = userService.login(loginRequest);

        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
    }

    /**
     * Test that login fails with invalid credentials.
     */
    @Test
    void loginThrowsExceptionWhenCredentialsAreInvalid() {
        given(userRepository.findByEmail(loginRequest.getEmail())).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash())).willReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.login(loginRequest));
    }

    /**
     * Test that login fails when user email doesn't exist.
     */
    @Test
    void loginThrowsExceptionWhenUserNotFound() {
        given(userRepository.findByEmail(loginRequest.getEmail())).willReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.login(loginRequest));
    }

    /**
     * Test retrieving user profile successfully.
     */
    @Test
    void getUserProfileReturnsUserData() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        UserResponse result = userService.getUserProfile(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("john@example.com", result.getEmail());
    }

    /**
     * Test that getUserProfile throws exception when user not found.
     */
    @Test
    void getUserProfileThrowsExceptionWhenNotFound() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThrows(com.cms.exception.ResourceNotFoundException.class, () -> userService.getUserProfile(99L));
    }

    /**
     * Test successful password change.
     */
    @Test
    void changePasswordUpdatesHashWhenCurrentPasswordMatches() {
        UserChangePasswordRequest request = new UserChangePasswordRequest();
        request.setCurrentPassword("password123");
        request.setNewPassword("newPassword123");

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("password123", testUser.getPasswordHash())).willReturn(true);
        given(passwordEncoder.encode("newPassword123")).willReturn("$2a$10$newHash");

        userService.changePassword(1L, request);

        verify(userRepository).save(any(User.class));
    }

    /**
     * Test password change fails when current password is wrong.
     */
    @Test
    void changePasswordThrowsWhenCurrentPasswordDoesNotMatch() {
        UserChangePasswordRequest request = new UserChangePasswordRequest();
        request.setCurrentPassword("wrong");
        request.setNewPassword("newPassword123");

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("wrong", testUser.getPasswordHash())).willReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.changePassword(1L, request));
    }
}
