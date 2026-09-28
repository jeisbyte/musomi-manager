package com.musomi.desktop.model.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Authentication response payload returned on successful POST /auth/login.
 *
 * @param token     JWT bearer token
 * @param expiresAt token expiration timestamp in UTC
 * @param user      authenticated user profile details
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginResponse(
    @JsonProperty("token")
    String token,

    @JsonProperty("expiresAt")
    Instant expiresAt,

    @JsonProperty("user")
    UserResponse user
) {

    public String getToken() {
        return token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public UserResponse getUser() {
        return user;
    }
}
