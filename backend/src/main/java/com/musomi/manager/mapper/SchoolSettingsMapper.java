package com.musomi.manager.mapper;

import com.musomi.manager.dto.response.SchoolSettingsResponse;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.SchoolSettings;
import com.musomi.manager.entity.Term;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Maps school settings and school details to response DTOs. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchoolSettingsMapper {

    /** Maps school settings to a response DTO, including school and optional current-term details. */
    public static SchoolSettingsResponse toResponse(SchoolSettings settings, School school) {
        Term currentTerm = settings != null ? settings.getCurrentTerm() : null;
        return new SchoolSettingsResponse(
                settings != null ? settings.getId() : null,
                school != null ? school.getId() : null,
                school != null ? school.getName() : null,
                school != null ? school.getLogoUrl() : null,
                school != null ? school.getAddress() : null,
                school != null ? school.getPhone() : null,
                school != null ? school.getEmail() : null,
                settings != null ? settings.getGradingScale() : null,
                settings != null ? settings.getReportHeader() : null,
                settings != null ? settings.getReportFooter() : null,
                currentTerm != null ? currentTerm.getId() : null,
                currentTerm != null && currentTerm.getAcademicYear() != null
                        ? currentTerm.getAcademicYear().getId()
                        : null
        );
    }
}
