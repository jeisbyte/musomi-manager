package com.musomi.manager.dto.response;

import java.time.LocalDateTime;

/** Import job status and row counts returned by the API. */
public record ImportJobResponse(
        Long id,
        String jobReference,
        String importType,
        String status,
        Integer totalRows,
        Integer validRows,
        Integer errorRows,
        String originalFilename,
        LocalDateTime createdAt,
        LocalDateTime confirmedAt
) {}
