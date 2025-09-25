package com.clyrafy.wallet.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final long refreshTokenExpiry = 1000 * 60 * 60;

    private UUID userId;
    private String email;
    private String role;

    @BeforeEach
    void setUp() {
        long accessTokenExpiry = 1000 * 60;
        String secret = "5c700634037e233223aa6fa5bb86c848f517f0541291ae92879e7e2ac58da576";
        jwtUtil = new JwtUtil(secret, accessTokenExpiry, refreshTokenExpiry);

        userId = UUID.randomUUID();
        email = "john@example.com";
        role = "ROLE_USER";
    }

    @Test
    public void generateAccessToken_shouldContainUserIdEmailAndRole() {
        String token = jwtUtil.generateAccessToken(userId, email, role);

        assertThat(jwtUtil.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtUtil.extractEmail(token)).isEqualTo(email);
        assertThat(jwtUtil.extractRole(token)).isEqualTo(role);
    }

    @Test
    public void generateRefreshToken_shouldContainUserIdAndEmail() {
        String token = jwtUtil.generateRefreshToken(userId, email);

        assertThat(jwtUtil.extractUserId(token)).isEqualTo(userId);
        assertThat(jwtUtil.extractEmail(token)).isEqualTo(email);
        assertThat(jwtUtil.extractRole(token)).isNull();
    }

    @Test
    public void validateToken_shouldReturnTrueForValidToken() {
        String token = jwtUtil.generateAccessToken(userId, email, role);

        boolean result = jwtUtil.validateToken(token);

        assertThat(result).isTrue();
    }

    @Test
    public void validateToken_shouldReturnFalseForMalformedToken() {
        String badToken = "invalid.jwt.token";

        boolean result = jwtUtil.validateToken(badToken);

        assertThat(result).isFalse();
    }

    @Test
    public void extractUserIdAsString_shouldReturnCorrectUUIDString() {
        String token = jwtUtil.generateAccessToken(userId, email, role);

        String extracted = jwtUtil.extractUserIdAsString(token);

        assertThat(extracted).isEqualTo(userId.toString());
    }

    @Test
    public void extractEmail_shouldReturnCorrectEmail() {
        String token = jwtUtil.generateAccessToken(userId, email, role);

        String extractedEmail = jwtUtil.extractEmail(token);

        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    public void extractRole_shouldReturnCorrectRole() {
        String token = jwtUtil.generateAccessToken(userId, email, role);

        String extractedRole = jwtUtil.extractRole(token);

        assertThat(extractedRole).isEqualTo(role);
    }

    @Test
    void extractUserId_shouldReturnCorrectUUID() {
        String token = jwtUtil.generateAccessToken(userId, email, role);

        UUID extractedId = jwtUtil.extractUserId(token);

        assertThat(extractedId).isEqualTo(userId);
    }

    @Test
    public void getRefreshTokenExpiryMs_shouldReturnConfiguredExpiry() {
        assertThat(jwtUtil.getRefreshTokenExpiryMs()).isEqualTo(refreshTokenExpiry);
    }
}
