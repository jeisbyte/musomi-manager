package com.musomi.manager.mapper;

import java.util.List;

import com.musomi.manager.dto.response.AcademicYearResponse;
import com.musomi.manager.entity.AcademicYear;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Maps academic year entities to response DTOs. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AcademicYearMapper {

    /** Maps an academic year to its response DTO. */
    public static AcademicYearResponse toResponse(AcademicYear academicYear) {
        if (academicYear == null) {
            return null;
        }
        return new AcademicYearResponse(
                academicYear.getId(),
                academicYear.getYear(),
                academicYear.getIsCurrent(),
                academicYear.getCreatedAt()
        );
    }

    /** Maps a list of academic years to response DTOs. */
    public static List<AcademicYearResponse> toResponseList(List<AcademicYear> academicYears) {
        return academicYears.stream()
                .map(AcademicYearMapper::toResponse)
                .toList();
    }
}
