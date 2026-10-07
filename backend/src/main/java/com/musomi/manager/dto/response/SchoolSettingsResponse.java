package com.musomi.manager.dto.response;

public record SchoolSettingsResponse(
        Long id,
        Long schoolId,
        String schoolName,
        String logoUrl,
        String address,
        String phone,
        String email,
        String gradingScale,
        String reportHeader,
        String reportFooter,
        Long currentTermId,
        Long currentAcademicYearId
) {
}
