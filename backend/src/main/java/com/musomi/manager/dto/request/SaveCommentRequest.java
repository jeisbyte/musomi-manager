package com.musomi.manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaveCommentRequest(
        @NotNull
        Long studentId,

        @NotNull
        Long termId,

        Long subjectId,

        @NotBlank
        String commentType,

        @NotBlank
        String commentText
) {
}
