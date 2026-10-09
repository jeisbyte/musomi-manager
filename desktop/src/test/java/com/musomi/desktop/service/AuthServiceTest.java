package com.musomi.desktop.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.musomi.desktop.api.ApiException;
import com.musomi.desktop.api.AuthApi;
import com.musomi.desktop.config.Session;
import com.musomi.desktop.model.dto.LoginResponse;
import com.musomi.desktop.model.dto.UserResponse;

class AuthServiceTest {

    private final Session session = Session.getInstance();

    @BeforeEach
    void startUnauthenticated() {
        session.clear();
    }

    @AfterEach
    void clearSession() {
        session.clear();
    }

    @Test
    void successfulLoginEstablishesSession() {
        AuthApi api = mock(AuthApi.class);
        when(api.login(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new LoginResponse("test-token", Instant.now().plusSeconds(60), teacher()));

        new AuthService(api, session).login("teacher", "password");

        assertTrue(session.isAuthenticated());
        assertEquals("TEACHER", session.getRole());
    }

    @Test
    void invalidCredentialsDoNotEstablishSession() {
        AuthApi api = mock(AuthApi.class);
        when(api.login(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new ApiException("INVALID_CREDENTIALS", "Invalid username or password."));

        assertThrows(ApiException.class,
                () -> new AuthService(api, session).login("teacher", "wrong-password"));

        assertFalse(session.isAuthenticated());
    }

    @Test
    void incompleteLoginResponseDoesNotEstablishSession() {
        AuthApi api = mock(AuthApi.class);
        when(api.login(org.mockito.ArgumentMatchers.any())).thenReturn(null);

        assertThrows(ApiException.class,
                () -> new AuthService(api, session).login("teacher", "password"));

        assertFalse(session.isAuthenticated());
    }

    @Test
    void expiredLoginResponseDoesNotEstablishSession() {
        AuthApi api = mock(AuthApi.class);
        when(api.login(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new LoginResponse("test-token", Instant.now().minusSeconds(1), teacher()));

        assertThrows(ApiException.class,
                () -> new AuthService(api, session).login("teacher", "password"));

        assertFalse(session.isAuthenticated());
    }

    private UserResponse teacher() {
        return new UserResponse(1L, "teacher", "Teacher", null, null, "TEACHER", 1L, "School");
    }
}
