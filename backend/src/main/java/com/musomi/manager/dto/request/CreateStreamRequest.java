package com.musomi.manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Request to create a stream. */
public record CreateStreamRequest(

        @NotNull
        Long classId,

        @NotBlank
        @Size(max = 50)
        String name,

        Integer capacity
) {
}
