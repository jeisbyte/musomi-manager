package com.musomi.desktop.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.musomi.desktop.model.dto.LoginRequest;
import com.musomi.desktop.model.dto.LoginResponse;
import com.musomi.desktop.model.dto.UserResponse;

/**
 * API client for authentication endpoints.
 *
 * <p>Covers the three auth endpoints defined in API_CONTRACT.md section 5:
 * <ul>
 *   <li>POST /auth/login  — obtain a JWT token</li>
 *   <li>GET  /auth/me     — fetch the currently authenticated user</li>
 *   <li>POST /auth/logout — invalidate the current session/token</li>
 * </ul>
 *
 * <p>Uses the {@link ApiClient} singleton for all HTTP communication.
 */
public class AuthApi {

    private static final Logger log = LoggerFactory.getLogger(AuthApi.class);

    private static final String LOGIN_PATH  = "/auth/login";
    private static final String ME_PATH     = "/auth/me";
    private static final String LOGOUT_PATH = "/auth/logout";

    private final ApiClient client;

    /**
     * Creates an {@code AuthApi} backed by the shared {@link ApiClient} singleton.
     */
    public AuthApi() {
        this.client = ApiClient.getInstance();
    }

    // -------------------------------------------------------------------------
    // Endpoints
    // -------------------------------------------------------------------------

    /**
     * Authenticates a user and returns a JWT token with profile data.
     *
     * <p>Corresponds to: {@code POST /auth/login}
     *
     * @param request credentials (username + password)
     * @return {@link LoginResponse} containing the bearer token, expiry, and user profile
     * @throws ApiException on HTTP 401 (invalid credentials), 403 (locked/inactive), or network error
     */
    public LoginResponse login(LoginRequest request) {
        log.debug("Attempting login for user: {}", request.getUsername());
        return client.post(LOGIN_PATH, request, LoginResponse.class);
    }

    /**
     * Returns the profile of the currently authenticated user.
     *
     * <p>Corresponds to: {@code GET /auth/me}
     *
     * @return {@link UserResponse} for the bearer-token holder
     * @throws ApiException on HTTP 401 or network error
     */
    public UserResponse me() {
        log.debug("Fetching current user profile");
        return client.get(ME_PATH, UserResponse.class);
    }

    /**
     * Invalidates the current session/token on the server.
     *
     * <p>Corresponds to: {@code POST /auth/logout} — responds with 204, no body.
     *
     * @throws ApiException on HTTP 401 or network error
     */
    public void logout() {
        log.debug("Sending logout request");
        client.post(LOGOUT_PATH, null, Void.class);
    }
}
