package com.musomi.manager.mapper;

import java.util.List;

import com.musomi.manager.dto.response.SubjectResponse;
import com.musomi.manager.entity.Subject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Maps subject entities to response DTOs. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SubjectMapper {

    /** Maps a subject to its response DTO. */
    public static SubjectResponse toResponse(Subject subject) {
        if (subject == null) {
            return null;
        }
        return new SubjectResponse(
                subject.getId(),
                subject.getCode(),
                subject.getName(),
                subject.getDescription(),
                subject.getIsCore(),
                subject.getIsActive()
        );
    }

    /** Maps a list of subjects to response DTOs. */
    public static List<SubjectResponse> toResponseList(List<Subject> subjects) {
        return subjects.stream()
                .map(SubjectMapper::toResponse)
                .toList();
    }
}
