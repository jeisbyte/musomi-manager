package com.musomi.desktop.config;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.musomi.desktop.model.dto.UserResponse;

/**
 * Thread-safe singleton managing the currently authenticated user's session state.
 *
 * <p>Stores the active JWT token, token expiration timestamp, and user profile.
 * Security notice: Session token values are sensitive and are never logged.
 */
public final class Session {

    private static final Logger log = LoggerFactory.getLogger(Session.class);


    // Singleton (initialization-on-demand holder)


    private static final class Holder {
        private static final Session INSTANCE = new Session();
    }

    private Session() {}

    /**
     * Returns the application-wide singleton instance.
     *
     * @return the {@code Session} singleton
     */
    public static Session getInstance() {
        return Holder.INSTANCE;
    }

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private volatile String token;
    private volatile Instant expiresAt;
    private volatile UserResponse user;


    // Session Lifecycle


    /**
     * Stores the authenticated session details.
     *
     * @param token     JWT authentication token
     * @param expiresAt token expiration timestamp in UTC
     * @param user      authenticated user profile
     */
    public void set(String token, Instant expiresAt, UserResponse user) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.user = user;

        if (log.isDebugEnabled()) {
            log.debug("Session established for user: {} (role: {}, expiresAt: {})",
                    user != null ? user.getUsername() : "null",
                    user != null ? user.getRole() : "null",
                    expiresAt);
        }
    }

    /**
     * Checks if a valid session exists.
     *
     * @return {@code true} if token is non-null and has not expired; {@code false} otherwise
     */
    public boolean isAuthenticated() {
        return token != null && expiresAt != null && expiresAt.isAfter(Instant.now());
    }

    /**
     * Resets all session fields to null, ending the session.
     */
    public void clear() {
        this.token = null;
        this.expiresAt = null;
        this.user = null;

        log.debug("Session cleared");
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Returns the active JWT token.
     *
     * @return the token string, or {@code null} if unauthenticated
     */
    public String getToken() {
        return token;
    }

    /**
     * Returns the timestamp when the current session expires.
     *
     * @return token expiration {@link Instant}, or {@code null}
     */
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /**
     * Returns the authenticated user profile.
     *
     * @return {@link UserResponse}, or {@code null} if unauthenticated
     */
    public UserResponse getUser() {
        return user;
    }

    // -------------------------------------------------------------------------
    // Convenience Delegations
    // -------------------------------------------------------------------------

    /**
     * Returns the current authenticated user's ID.
     *
     * @return user ID, or {@code null} if no user is present
     */
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    /**
     * Returns the current authenticated user's role.
     *
     * @return role name (e.g. "ADMIN", "TEACHER"), or {@code null} if no user is present
     */
    public String getRole() {
        return user != null ? user.getRole() : null;
    }

    /**
     * Returns the school ID associated with the current authenticated user.
     *
     * @return school ID, or {@code null} if no user is present
     */
    public Long getSchoolId() {
        return user != null ? user.getSchoolId() : null;
    }
}
