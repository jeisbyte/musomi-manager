package com.musomi.manager.dto.response;

/** Class level details returned to clients. */
public record ClassLevelResponse(
        Long id,
        String name,
        Integer sortOrder,
        Boolean isActive
) {
}
