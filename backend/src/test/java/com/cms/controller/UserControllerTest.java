package com.cms.controller;

import com.cms.dto.UserLoginRequest;
import com.cms.dto.UserRegistrationRequest;
import com.cms.dto.UserResponse;
import com.cms.service.UserService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for the UserController.
 * Tests user registration, login, and profile endpoints.
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    /**
     * Test successful user registration.
     */
    @Test
    void registerCreatesNewUserAndReturns201() throws Exception {
        UserResponse userResponse = createUserResponse();
        given(userService.register(any(UserRegistrationRequest.class))).willReturn(userResponse);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "fullName": "John Doe",
                      "email": "john@example.com",
                      "password": "password123",
                      "phone": "5551234567"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("john@example.com"))
            .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    /**
     * Test registration fails when email is invalid.
     */
    @Test
    void registerFailsWithInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "fullName": "John Doe",
                      "email": "invalid-email",
                      "password": "password123"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    /**
     * Test registration fails when password is too short.
     */
    @Test
    void registerFailsWithShortPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "fullName": "John Doe",
                      "email": "john@example.com",
                      "password": "123"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    /**
     * Test successful user login.
     */
    @Test
    void loginReturnsUserWhenCredentialsAreValid() throws Exception {
        UserResponse userResponse = createUserResponse();
        given(userService.login(any(UserLoginRequest.class))).willReturn(userResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "john@example.com",
                      "password": "password123"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    /**
     * Test login fails with invalid credentials.
     */
    @Test
    void loginFailsWhenCredentialsAreInvalid() throws Exception {
        given(userService.login(any(UserLoginRequest.class)))
            .willThrow(new IllegalArgumentException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "john@example.com",
                      "password": "wrongpassword"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    /**
     * Test retrieving user profile successfully.
     */
    @Test
    void getProfileReturnsUserData() throws Exception {
        UserResponse userResponse = createUserResponse();
        given(userService.getUserProfile(1L)).willReturn(userResponse);

        mockMvc.perform(get("/api/auth/profile/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("john@example.com"));
    }

              /**
               * Test changing password successfully.
               */
              @Test
              void changePasswordReturnsNoContent() throws Exception {
            doNothing().when(userService).changePassword(org.mockito.ArgumentMatchers.eq(1L), any());

            mockMvc.perform(post("/api/auth/profile/1/change-password")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {
                    "currentPassword": "password123",
                    "newPassword": "newPassword123"
                  }
                  """))
                .andExpect(status().isNoContent());
              }

              /**
               * Test changing password fails when new password is too short.
               */
              @Test
              void changePasswordFailsValidation() throws Exception {
            mockMvc.perform(post("/api/auth/profile/1/change-password")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {
                    "currentPassword": "password123",
                    "newPassword": "123"
                  }
                  """))
                .andExpect(status().isBadRequest());
              }

    /**
     * Added after reviewing yesterday's change-password task:
     * business rule violations should come back as structured 400 errors.
     */
    @Test
    void changePasswordReturnsBadRequestWhenCurrentPasswordIsWrong() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalArgumentException("Current password is incorrect"))
            .when(userService).changePassword(org.mockito.ArgumentMatchers.eq(1L), any());

        mockMvc.perform(post("/api/auth/profile/1/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "currentPassword": "wrong-password",
                      "newPassword": "newPassword123"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Current password is incorrect"))
            .andExpect(jsonPath("$.path").value("/api/auth/profile/1/change-password"));
    }

    /**
     * Helper method to create a sample UserResponse for testing.
     */
    private UserResponse createUserResponse() {
        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setFullName("John Doe");
        response.setEmail("john@example.com");
        response.setPhone("5551234567");
        response.setCreatedAt(LocalDateTime.of(2026, 4, 25, 10, 0));
        return response;
    }
}
