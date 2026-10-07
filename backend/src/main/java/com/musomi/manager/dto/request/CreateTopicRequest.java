package com.musomi.manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Request to create a topic. */
public record CreateTopicRequest(

        @NotNull
        Long subjectId,

        @NotBlank
        @Size(max = 150)
        String name,

        String description,

        @NotNull
        Integer sortOrder
) {
}
