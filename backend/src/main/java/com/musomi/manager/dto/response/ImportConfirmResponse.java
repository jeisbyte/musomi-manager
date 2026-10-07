package com.musomi.manager.dto.response;

public record ImportConfirmResponse(
        Integer imported,
        Integer skipped
) {
}
