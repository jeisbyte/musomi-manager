package com.musomi.manager.service;

import com.musomi.manager.dto.request.LoginRequest;
import com.musomi.manager.dto.response.LoginResponse;
import com.musomi.manager.dto.response.UserSummary;
import com.musomi.manager.entity.User;
import com.musomi.manager.entity.UserSession;
import com.musomi.manager.exception.AuthenticationException;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.repository.UserRepository;
import com.musomi.manager.repository.UserSessionRepository;
import com.musomi.manager.security.JwtTokenProvider;
import com.musomi.manager.util.TokenHasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /** Authenticates a user and issues a recorded JWT session. */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findBySchoolIdAndUsername(1L, request.username())
                .orElseThrow(() -> new AuthenticationException(ErrorCode.INVALID_CREDENTIALS));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AuthenticationException(ErrorCode.ACCOUNT_INACTIVE);
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new AuthenticationException(ErrorCode.ACCOUNT_LOCKED);
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            int attempts = (user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0) + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
                log.warn("User account locked due to 5 failed login attempts: userId={}, username={}",
                        user.getId(), user.getUsername());
            }
            userRepository.save(user);
            throw new AuthenticationException(ErrorCode.INVALID_CREDENTIALS);
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user);
        Instant expiresAt = jwtTokenProvider.getExpiration(token);

        UserSummary summary = new UserSummary(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole().name(),
                user.getSchool().getId(),
                user.getSchool().getName()
        );
        String tokenHash = TokenHasher.hash(token);
        userSessionRepository.save(UserSession.builder()
                .school(user.getSchool())
                .user(user)
                .tokenHash(tokenHash)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.ofInstant(expiresAt, ZoneOffset.UTC))
                .build());
        return new LoginResponse(token, expiresAt, summary);
    }

    /** Revokes the session associated with the given raw JWT. */
    @Transactional
    public void logout(String rawToken) {
        String tokenHash = TokenHasher.hash(rawToken);
        userSessionRepository.findByTokenHash(tokenHash)
                .ifPresent(session -> {
                    if (session.getRevokedAt() == null) {
                        session.setRevokedAt(LocalDateTime.now());
                        userSessionRepository.save(session);
                        log.info("Session revoked for user {}", session.getUser().getId());
                    }
                });
    }

    public UserSummary getUserSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException(ErrorCode.SESSION_EXPIRED));
        return new UserSummary(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole().name(),
                user.getSchool().getId(),
                user.getSchool().getName()
        );
    }
}
