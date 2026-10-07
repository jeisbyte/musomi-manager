package com.musomi.manager.dto.response;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long studentId,
        Long termId,
        Long subjectId,
        String subjectName,
        String commentType,
        String commentText,
        Long writtenBy,
        String writtenByName,
        LocalDateTime writtenAt
) {
}
