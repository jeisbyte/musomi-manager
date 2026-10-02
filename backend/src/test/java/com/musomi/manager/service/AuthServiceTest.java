package com.musomi.manager.service;

import com.musomi.manager.dto.request.LoginRequest;
import com.musomi.manager.dto.response.LoginResponse;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.User;
import com.musomi.manager.entity.enums.Role;
import com.musomi.manager.exception.AuthenticationException;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.repository.UserRepository;
import com.musomi.manager.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Returns a token when credentials are valid")
    void shouldReturnTokenWhenCredentialsValid() {
        User user = createUser();
        Instant expiresAt = Instant.parse("2026-09-30T12:00:00Z");
        when(userRepository.findBySchoolIdAndUsername(1L, "jane")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", "hashed-password")).thenReturn(true);
        when(jwtTokenProvider.generateToken(user)).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpiration("jwt-token")).thenReturn(expiresAt);

        LoginResponse response = authService.login(new LoginRequest("jane", "correct-password"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.expiresAt()).isEqualTo(expiresAt);
        assertThat(response.user().id()).isEqualTo(42L);
        assertThat(response.user().username()).isEqualTo("jane");
        assertThat(response.user().fullName()).isEqualTo("Jane Doe");
        assertThat(response.user().role()).isEqualTo("ADMIN");
        assertThat(response.user().schoolId()).isEqualTo(1L);
        assertThat(response.user().schoolName()).isEqualTo("Musomi School");
        assertThat(response.user().schoolName()).isEqualTo("Musomi School");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Throws when the username is not found")
    void shouldThrowWhenUsernameNotFound() {
        when(userRepository.findBySchoolIdAndUsername(1L, "missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("missing", "password")))
                .isInstanceOf(AuthenticationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);

        verify(passwordEncoder, never()).matches("password", "hashed-password");
    }

    @Test
    @DisplayName("Throws and increments failed attempts when the password is wrong")
    void shouldThrowWhenPasswordWrong() {
        User user = createUser();
        when(userRepository.findBySchoolIdAndUsername(1L, "jane")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("jane", "wrong-password")))
                .isInstanceOf(AuthenticationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);

        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Locks the account for about 15 minutes after five failed attempts")
    void shouldLockAccountAfterFiveFailedAttempts() {
        User user = createUser();
        user.setFailedLoginAttempts(4);
        when(userRepository.findBySchoolIdAndUsername(1L, "jane")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("jane", "wrong-password")))
                .isInstanceOf(AuthenticationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);

        LocalDateTime now = LocalDateTime.now();
        assertThat(user.getFailedLoginAttempts()).isEqualTo(5);
        assertThat(user.getLockedUntil())
                .isBetween(now.plusMinutes(15).minusSeconds(1), now.plusMinutes(15).plusSeconds(1));
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Throws when the account is locked")
    void shouldThrowWhenAccountLocked() {
        User user = createUser();
        user.setLockedUntil(LocalDateTime.now().plusMinutes(5));
        when(userRepository.findBySchoolIdAndUsername(1L, "jane")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("jane", "correct-password")))
                .isInstanceOf(AuthenticationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_LOCKED);

        verify(passwordEncoder, never()).matches("correct-password", "hashed-password");
    }

    @Test
    @DisplayName("Throws when the account is inactive")
    void shouldThrowWhenAccountInactive() {
        User user = createUser();
        user.setIsActive(false);
        when(userRepository.findBySchoolIdAndUsername(1L, "jane")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("jane", "correct-password")))
                .isInstanceOf(AuthenticationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_INACTIVE);

        verify(passwordEncoder, never()).matches("correct-password", "hashed-password");
    }

    @Test
    @DisplayName("Resets failed attempts and lockout after successful login")
    void shouldResetFailedAttemptsOnSuccess() {
        User user = createUser();
        user.setFailedLoginAttempts(4);
        user.setLockedUntil(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findBySchoolIdAndUsername(1L, "jane")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", "hashed-password")).thenReturn(true);
        when(jwtTokenProvider.generateToken(user)).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpiration("jwt-token")).thenReturn(Instant.now().plusSeconds(3600));

        LocalDateTime beforeLogin = LocalDateTime.now();
        authService.login(new LoginRequest("jane", "correct-password"));

        assertThat(user.getFailedLoginAttempts()).isZero();
        assertThat(user.getLockedUntil()).isNull();
        assertThat(user.getLastLoginAt()).isBetween(beforeLogin, LocalDateTime.now());
        verify(userRepository).save(user);
    }

    private User createUser() {
        return User.builder()
                .id(42L)
                .school(School.builder().id(1L).name("Musomi School").build())
                .username("jane")
                .passwordHash("hashed-password")
                .fullName("Jane Doe")
                .role(Role.ADMIN)
                .isActive(true)
                .failedLoginAttempts(0)
                .build();
    }
}
