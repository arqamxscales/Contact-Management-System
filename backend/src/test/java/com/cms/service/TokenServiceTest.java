package com.cms.service;

import com.cms.dto.RefreshTokenRequest;
import com.cms.dto.TokenResponse;
import com.cms.entity.RefreshToken;
import com.cms.entity.User;
import com.cms.exception.UnauthorizedException;
import com.cms.repository.RefreshTokenRepository;
import com.cms.util.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TokenService - JWT token lifecycle management.
 * Tests token generation, refresh, and revocation flows.
 */
@DisplayName("TokenService Tests")
class TokenServiceTest {

    // Test data constants
    private static final String TEST_EMAIL = "user@example.com";
    private static final String TEST_ACCESS_TOKEN = "access-jwt-1";
    private static final String TEST_REFRESH_TOKEN = "refresh-jwt-1";
    private static final String TEST_NEW_ACCESS_TOKEN = "access-jwt-2";
    private static final String INVALID_TOKEN = "invalid_token";

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TokenService tokenService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should generate both access and refresh tokens on login")
    void testGenerateTokens() {
        // Arrange
        when(jwtTokenProvider.generateToken(1L, TEST_EMAIL))
            .thenReturn(TEST_ACCESS_TOKEN);
        when(jwtTokenProvider.generateRefreshToken(1L))
            .thenReturn(TEST_REFRESH_TOKEN);
        when(refreshTokenRepository.save(any(RefreshToken.class)))
            .thenAnswer(invocation -> {
                RefreshToken token = invocation.getArgument(0);
                token.setId(1L);
                return token;
            });

        // Act
        TokenResponse response = tokenService.generateTokens(testUser);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_ACCESS_TOKEN, response.getAccessToken());
        assertEquals(TEST_REFRESH_TOKEN, response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(900L, response.getExpiresIn());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should refresh access token with valid refresh token")
    void testRefreshAccessToken() {
        // Arrange
        RefreshToken validToken = new RefreshToken();
        validToken.setId(1L);
        validToken.setToken(TEST_REFRESH_TOKEN);
        validToken.setUser(testUser);
        validToken.setRevoked(false);
        validToken.setExpiresAt(LocalDateTime.now().plusDays(7));

        RefreshTokenRequest request = new RefreshTokenRequest(TEST_REFRESH_TOKEN, TEST_ACCESS_TOKEN);

        when(refreshTokenRepository.findByToken(TEST_REFRESH_TOKEN))
            .thenReturn(Optional.of(validToken));
        when(jwtTokenProvider.generateToken(1L, TEST_EMAIL))
            .thenReturn(TEST_NEW_ACCESS_TOKEN);

        // Act
        TokenResponse response = tokenService.refreshAccessToken(request);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_NEW_ACCESS_TOKEN, response.getAccessToken());
        assertEquals(TEST_REFRESH_TOKEN, response.getRefreshToken());
    }

    @Test
    @DisplayName("Should reject refresh with invalid token")
    void testRefreshWithInvalidToken() {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest(INVALID_TOKEN, TEST_ACCESS_TOKEN);
        when(refreshTokenRepository.findByToken(INVALID_TOKEN))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UnauthorizedException.class,
            () -> tokenService.refreshAccessToken(request));
    }

    @Test
    @DisplayName("Should reject refresh with revoked token")
    void testRefreshWithRevokedToken() {
        // Arrange
        RefreshToken revokedToken = new RefreshToken();
        revokedToken.setToken(TEST_REFRESH_TOKEN);
        revokedToken.setUser(testUser);
        revokedToken.setRevoked(true);
        revokedToken.setExpiresAt(LocalDateTime.now().plusDays(7));

        RefreshTokenRequest request = new RefreshTokenRequest(TEST_REFRESH_TOKEN, TEST_ACCESS_TOKEN);

        when(refreshTokenRepository.findByToken(TEST_REFRESH_TOKEN))
            .thenReturn(Optional.of(revokedToken));

        // Act & Assert
        assertThrows(UnauthorizedException.class,
            () -> tokenService.refreshAccessToken(request));
    }

    @Test
    @DisplayName("Should reject refresh with expired token")
    void testRefreshWithExpiredToken() {
        // Arrange
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken(TEST_REFRESH_TOKEN);
        expiredToken.setUser(testUser);
        expiredToken.setRevoked(false);
        expiredToken.setExpiresAt(LocalDateTime.now().minusHours(1)); // Already expired

        RefreshTokenRequest request = new RefreshTokenRequest(TEST_REFRESH_TOKEN, TEST_ACCESS_TOKEN);

        when(refreshTokenRepository.findByToken(TEST_REFRESH_TOKEN))
            .thenReturn(Optional.of(expiredToken));

        // Act & Assert
        assertThrows(UnauthorizedException.class,
            () -> tokenService.refreshAccessToken(request));
    }

    @Test
    @DisplayName("Should revoke single refresh token on logout")
    void testRevokeRefreshToken() {
        // Arrange
        RefreshToken token = new RefreshToken();
        token.setId(1L);
        token.setToken(TEST_REFRESH_TOKEN);
        token.setUser(testUser);
        token.setRevoked(false);

        when(refreshTokenRepository.findByToken(TEST_REFRESH_TOKEN))
            .thenReturn(Optional.of(token));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
            .thenReturn(token);

        // Act
        tokenService.revokeRefreshToken(TEST_REFRESH_TOKEN);

        // Assert
        verify(refreshTokenRepository, times(1)).save(argThat(RefreshToken::getRevoked));
    }

    @Test
    @DisplayName("Should revoke all user tokens on password change")
    void testRevokeAllRefreshTokens() {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(testUser);

        // Act
        tokenService.revokeAllRefreshTokens(1L);

        // Assert
        verify(refreshTokenRepository, times(1)).updateRevokedTrueByUser(testUser);
    }
}
