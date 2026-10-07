package com.musomi.manager.dto.response;

import java.util.List;

public record ImportPreviewResponse(
        String importId,
        Integer totalRows,
        Integer validRows,
        Integer errorRows,
        List<ImportRowError> errors
) {
    public record ImportRowError(int row, String field, String message) {
    }
}
