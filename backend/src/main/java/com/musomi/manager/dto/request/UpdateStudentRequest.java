package com.musomi.manager.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateStudentRequest(

        @NotBlank
        @Size(max = 200)
        String fullName,

        @Size(max = 10)
        String gender,

        LocalDate dateOfBirth,

        Long currentClassId,

        Long currentStreamId,

        @Size(max = 20)
        String status
) {
}
