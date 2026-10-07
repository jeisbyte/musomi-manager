package com.musomi.manager.dto.response;

public record GuardianResponse(
        Long id,
        String name,
        String relationship,
        String phone,
        String email,
        Boolean isPrimary
) {
}
