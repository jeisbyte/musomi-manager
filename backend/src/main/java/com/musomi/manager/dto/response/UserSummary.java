package com.musomi.manager.dto.response;

public record UserSummary(
        Long id,
        String username,
        String fullName,
        String role,
        Long schoolId,
        String schoolName
) {
}