package com.musomi.manager.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.musomi.manager.entity.School;
import com.musomi.manager.entity.User;
import com.musomi.manager.entity.enums.Role;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private User user;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(
                jwtTokenProvider, "secret", "test-secret-that-is-at-least-32-characters-long-for-hs256");
        ReflectionTestUtils.setField(jwtTokenProvider, "expirationMs", 3600000L);

        School school = School.builder()
                .id(7L)
                .name("Test School")
                .build();
        user = User.builder()
                .id(42L)
                .school(school)
                .username("testuser")
                .role(Role.TEACHER)
                .build();
    }

    @Test
    @DisplayName("Should generate a token for a valid user")
    void shouldGenerateTokenForValidUser() {
        String token = jwtTokenProvider.generateToken(user);

        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("Should extract the user ID from a token")
    void shouldExtractUserIdFromToken() {
        String token = jwtTokenProvider.generateToken(user);

        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(42L);
    }

    @Test
    @DisplayName("Should extract the school ID from a token")
    void shouldExtractSchoolIdFromToken() {
        String token = jwtTokenProvider.generateToken(user);

        assertThat(jwtTokenProvider.getSchoolId(token)).isEqualTo(7L);
    }

    @Test
    @DisplayName("Should extract the role from a token")
    void shouldExtractRoleFromToken() {
        String token = jwtTokenProvider.generateToken(user);

        assertThat(jwtTokenProvider.getRole(token)).isEqualTo("TEACHER");
    }

    @Test
    @DisplayName("Should extract the username from a token")
    void shouldExtractUsernameFromToken() {
        String token = jwtTokenProvider.generateToken(user);

        assertThat(jwtTokenProvider.getUsername(token)).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should return true when a token is valid")
    void shouldReturnTrueWhenTokenIsValid() {
        String token = jwtTokenProvider.generateToken(user);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Should return false when a token is tampered")
    void shouldReturnFalseWhenTokenIsTampered() {
        String token = jwtTokenProvider.generateToken(user);
        char lastCharacter = token.charAt(token.length() - 1);
        char replacement = lastCharacter == 'A' ? 'B' : 'A';
        String tamperedToken = token.substring(0, token.length() - 1) + replacement;

        assertThat(jwtTokenProvider.validateToken(tamperedToken)).isFalse();
    }

    @Test
    @DisplayName("Should return false when a token is garbage")
    void shouldReturnFalseWhenTokenIsGarbage() {
        assertThat(jwtTokenProvider.validateToken("not.a.jwt")).isFalse();
    }

    @Test
    @DisplayName("Should return false when a token is expired")
    void shouldReturnFalseWhenTokenIsExpired() {
        ReflectionTestUtils.setField(jwtTokenProvider, "expirationMs", -1000L);
        String token = jwtTokenProvider.generateToken(user);

        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("Should return a future expiration")
    void shouldReturnFutureExpiration() {
        String token = jwtTokenProvider.generateToken(user);

        assertThat(jwtTokenProvider.getExpiration(token)).isAfter(Instant.now());
    }
}
