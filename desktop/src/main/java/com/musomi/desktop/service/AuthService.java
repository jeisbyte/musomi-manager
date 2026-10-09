package com.musomi.desktop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.musomi.desktop.api.AuthApi;
import com.musomi.desktop.api.ApiException;
import com.musomi.desktop.config.Session;
import com.musomi.desktop.model.dto.LoginRequest;
import com.musomi.desktop.model.dto.LoginResponse;
import com.musomi.desktop.model.dto.UserResponse;

/**
 * Client-side service for authentication operations.
 *
 * <p>Orchestrates calls to {@link AuthApi} and maintains session state via
 * {@link Session}.  Controllers must never call {@link AuthApi} directly —
 * always go through this service.
 *
 * <p>Layer diagram (DESKTOP.md §4):
 * <pre>
 *   Controller → AuthService → AuthApi → ApiClient → Backend
 * </pre>
 */
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthApi api;
    private final Session session;

    /**
     * Creates an {@code AuthService} using the provided collaborators.
     *
     * @param api     authentication API wrapper
     * @param session singleton session store
     */
    public AuthService(AuthApi api, Session session) {
        this.api     = api;
        this.session = session;
    }

    /**
     * Convenience constructor that wires up the singletons automatically.
     */
    public AuthService() {
        this(new AuthApi(), Session.getInstance());
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Authenticates the user and persists the resulting session.
     *
     * <ol>
     *   <li>Calls {@code POST /auth/login} via {@link AuthApi#login}.</li>
     *   <li>Stores the token, expiry, and user profile in {@link Session}.</li>
     *   <li>Returns the full {@link LoginResponse} to the caller.</li>
     * </ol>
     *
     * @param username login username
     * @param password login password
     * @return the server's {@link LoginResponse} (token + user profile)
     * @throws com.musomi.desktop.api.ApiException on invalid credentials, locked account, or network error
     */
    public LoginResponse login(String username, String password) {
        log.debug("AuthService.login");

        LoginResponse response = api.login(new LoginRequest(username, password));
        if (response == null) {
            throw new ApiException("INVALID_AUTH_RESPONSE", "The server returned an invalid login response.");
        }

        try {
            session.set(response.getToken(), response.getExpiresAt(), response.getUser());
        } catch (IllegalArgumentException ex) {
            throw new ApiException("INVALID_AUTH_RESPONSE", "The server returned an invalid login response.");
        }
        log.debug("Session established");

        return response;
    }

    /**
     * Logs out the current user.
     *
     * <p>Calls {@code POST /auth/logout} in a {@code try/finally} block so that
     * {@link Session#clear()} is always invoked — even if the server request fails.
     *
     * @throws com.musomi.desktop.api.ApiException only if the server returns an unexpected error
     *         (session is still cleared locally regardless)
     */
    public void logout() {
        log.debug("AuthService.logout");
        try {
            api.logout();
        } finally {
            session.clear();
            log.debug("Session cleared after logout");
        }
    }

    /**
     * Returns the profile of the currently authenticated user.
     *
     * <p>Delegates directly to {@code GET /auth/me} via {@link AuthApi#me()}.
     * The caller is responsible for ensuring an active session exists before calling this.
     *
     * @return {@link UserResponse} for the bearer-token holder
     * @throws com.musomi.desktop.api.ApiException on HTTP 401 or network error
     */
    public UserResponse me() {
        log.debug("AuthService.me");
        return api.me();
    }
}
