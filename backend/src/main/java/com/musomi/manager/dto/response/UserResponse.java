package com.musomi.manager.dto.response;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String fullName,
        String email,
        String phone,
        String role,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt
) {
}