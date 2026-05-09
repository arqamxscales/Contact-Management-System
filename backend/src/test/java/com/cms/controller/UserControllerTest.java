package com.cms.controller;

import com.cms.dto.RefreshTokenRequest;
import com.cms.dto.TokenResponse;
import com.cms.dto.UserChangePasswordRequest;
import com.cms.dto.UserLoginRequest;
import com.cms.dto.UserRegistrationRequest;
import com.cms.dto.UserResponse;
import com.cms.service.TokenService;
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
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for the UserController.
 * Keeps the auth routes honest while the frontend session flow evolves.
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    private static final String AUTH_BASE = "/api/auth";
    private static final String REGISTER_ACTION = "register";
    private static final String LOGIN_ACTION = "login";
    private static final String REFRESH_ACTION = "refresh";
    private static final String LOGOUT_ALL_ACTION = "logout-all";
    private static final String PROFILE_USER_ID = "1";
    private static final String CHANGE_PASSWORD_SUFFIX = PROFILE_USER_ID + "/change-password";
    private static final String EMAIL = "john@example.com";
    private static final String EMAIL_JSON = "$.email";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private TokenService tokenService;

    @Test
    void registerCreatesNewUserAndReturns201() throws Exception {
        UserResponse userResponse = createUserResponse();
        given(userService.register(any(UserRegistrationRequest.class))).willReturn(userResponse);

        mockMvc.perform(post(authPath(REGISTER_ACTION))
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
            .andExpect(jsonPath(EMAIL_JSON).value(EMAIL))
            .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    @Test
    void registerFailsWithInvalidEmail() throws Exception {
        mockMvc.perform(post(authPath(REGISTER_ACTION))
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

    @Test
    void registerFailsWithShortPassword() throws Exception {
        mockMvc.perform(post(authPath(REGISTER_ACTION))
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

    @Test
    void loginReturnsUserWhenCredentialsAreValid() throws Exception {
        UserResponse userResponse = createUserResponse();
        given(userService.login(any(UserLoginRequest.class))).willReturn(userResponse);

        mockMvc.perform(post(authPath(LOGIN_ACTION))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "john@example.com",
                      "password": "password123"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath(EMAIL_JSON).value(EMAIL));
    }

    @Test
    void loginFailsWhenCredentialsAreInvalid() throws Exception {
        given(userService.login(any(UserLoginRequest.class)))
            .willThrow(new IllegalArgumentException("Invalid email or password"));

        mockMvc.perform(post(authPath(LOGIN_ACTION))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "john@example.com",
                      "password": "wrongpassword"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void refreshReturnsNewTokenPair() throws Exception {
        TokenResponse tokenResponse = new TokenResponse("access-2", "refresh-1", 900L);
        given(tokenService.refreshAccessToken(any(RefreshTokenRequest.class))).willReturn(tokenResponse);

        mockMvc.perform(post(authPath(REFRESH_ACTION))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "accessToken": "access-1",
                      "refreshToken": "refresh-1"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("access-2"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-1"));
    }

    @Test
    void logoutAllReturnsNoContent() throws Exception {
        doNothing().when(tokenService).revokeAllRefreshTokens(1L);

        mockMvc.perform(post(authPath(LOGOUT_ALL_ACTION))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "userId": 1
                    }
                    """))
            .andExpect(status().isNoContent());

        verify(tokenService).revokeAllRefreshTokens(1L);
    }

    @Test
    void getProfileReturnsUserData() throws Exception {
        UserResponse userResponse = createUserResponse();
        given(userService.getUserProfile(1L)).willReturn(userResponse);

        mockMvc.perform(get(profilePath(PROFILE_USER_ID)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath(EMAIL_JSON).value(EMAIL));
    }

    @Test
    void changePasswordReturnsNoContent() throws Exception {
        doNothing().when(userService).changePassword(org.mockito.ArgumentMatchers.eq(1L), any());

        mockMvc.perform(post(profilePath(CHANGE_PASSWORD_SUFFIX))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "currentPassword": "password123",
                      "newPassword": "newPassword123"
                    }
                    """))
            .andExpect(status().isNoContent());
    }

    @Test
    void changePasswordFailsValidation() throws Exception {
        mockMvc.perform(post(profilePath(CHANGE_PASSWORD_SUFFIX))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "currentPassword": "password123",
                      "newPassword": "123"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void changePasswordReturnsBadRequestWhenCurrentPasswordIsWrong() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalArgumentException("Current password is incorrect"))
            .when(userService).changePassword(org.mockito.ArgumentMatchers.eq(1L), any());

        mockMvc.perform(post(profilePath(CHANGE_PASSWORD_SUFFIX))
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
            .andExpect(jsonPath("$.path").value(profilePath(CHANGE_PASSWORD_SUFFIX)));
    }

    private UserResponse createUserResponse() {
        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setFullName("John Doe");
        response.setEmail(EMAIL);
        response.setPhone("5551234567");
        response.setCreatedAt(LocalDateTime.of(2026, 4, 25, 10, 0));
        return response;
    }

    private String authPath(String action) {
        return AUTH_BASE + "/" + action;
    }

    private String profilePath(String suffix) {
        return AUTH_BASE + "/profile/" + suffix;
    }
}
