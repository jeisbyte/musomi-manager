package com.musomi.manager.dto.request;

public record UpdateSchoolSettingsRequest(
        String gradingScale,
        String reportHeader,
        String reportFooter,
        Long currentTermId
) {
}
