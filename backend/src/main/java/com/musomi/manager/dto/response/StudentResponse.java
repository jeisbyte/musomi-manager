package com.musomi.manager.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record StudentResponse(
        Long id,
        String admissionNumber,
        String fullName,
        String gender,
        LocalDate dateOfBirth,
        Long currentClassId,
        String currentClassName,
        Long currentStreamId,
        String currentStreamName,
        String status,
        Boolean isActive,
        LocalDateTime createdAt,
        List<GuardianResponse> guardians
) {
}
