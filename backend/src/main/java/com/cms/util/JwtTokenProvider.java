package com.cms.util;

/**
 * Minimal JwtTokenProvider placeholder.
 * The unit tests mock this class, but having a concrete type avoids compilation errors.
 */
public class JwtTokenProvider {

    public String generateToken(long userId, String email) {
        return "token-" + userId + "-" + email;
    }

    public String generateRefreshToken(long userId) {
        return "refresh-" + userId;
    }

    public boolean validateToken(String token) {
        return token != null && !token.isBlank();
    }
}
