package com.musomi.manager.dto.request;

import jakarta.validation.constraints.NotNull;

public record GenerateReportRequest(
        @NotNull
        Long classId,

        Long streamId,

        @NotNull
        Long termId
) {
}
