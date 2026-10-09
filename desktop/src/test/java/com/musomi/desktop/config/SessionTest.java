package com.musomi.desktop.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.musomi.desktop.model.dto.UserResponse;

class SessionTest {

    private final Session session = Session.getInstance();

    @AfterEach
    void clearSession() {
        session.clear();
    }

    @Test
    void authenticatesOnlyWithCompleteUnexpiredSession() {
        session.set("test-token", Instant.now().plusSeconds(60), teacher());

        assertTrue(session.isAuthenticated());

        session.clear();
        assertFalse(session.isAuthenticated());
    }

    @Test
    void rejectsIncompleteOrExpiredSessionData() {
        assertThrows(IllegalArgumentException.class,
                () -> session.set("test-token", Instant.now().minusSeconds(1), teacher()));
        assertThrows(IllegalArgumentException.class,
                () -> session.set(" ", Instant.now().plusSeconds(60), teacher()));

        assertFalse(session.isAuthenticated());
    }

    private UserResponse teacher() {
        return new UserResponse(1L, "teacher", "Teacher", null, null, "TEACHER", 1L, "School");
    }
}
