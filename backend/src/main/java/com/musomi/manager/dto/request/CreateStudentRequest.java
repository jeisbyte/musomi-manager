package com.musomi.manager.dto.request;

import java.time.LocalDate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateStudentRequest(

        @NotBlank
        @Size(max = 50)
        String admissionNumber,

        @NotBlank
        @Size(max = 200)
        String fullName,

        @Size(max = 10)
        String gender,

        LocalDate dateOfBirth,

        Long currentClassId,

        Long currentStreamId,

        @Valid
        CreateGuardianRequest guardian
) {
}
