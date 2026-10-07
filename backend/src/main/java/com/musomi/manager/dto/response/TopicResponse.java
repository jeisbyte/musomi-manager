package com.musomi.manager.dto.response;

/** Topic details returned to clients. */
public record TopicResponse(
        Long id,
        Long subjectId,
        String name,
        String description,
        Integer sortOrder
) {
}
