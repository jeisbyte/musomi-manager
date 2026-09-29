package com.musomi.manager.dto.response;

import java.time.Instant;

public record LoginResponse(
        String token,
        Instant expiresAt,
        UserSummary user
) {
}
