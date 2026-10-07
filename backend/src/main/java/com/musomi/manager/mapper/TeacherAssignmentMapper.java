package com.musomi.manager.mapper;

import java.util.List;

import com.musomi.manager.dto.response.AssignmentResponse;
import com.musomi.manager.entity.TeacherAssignment;

/** Maps teacher assignments to response DTOs. */
public final class TeacherAssignmentMapper {

    private TeacherAssignmentMapper() {
    }

    /** Maps a teacher assignment to its response DTO. */
    public static AssignmentResponse toResponse(TeacherAssignment entity) {
        if (entity == null) {
            return null;
        }
        return new AssignmentResponse(
                entity.getId(),
                entity.getTeacher() != null ? entity.getTeacher().getId() : null,
                entity.getTeacher() != null ? entity.getTeacher().getFullName() : null,
                entity.getSubject() != null ? entity.getSubject().getId() : null,
                entity.getSubject() != null ? entity.getSubject().getName() : null,
                entity.getClassEntity() != null ? entity.getClassEntity().getId() : null,
                entity.getClassEntity() != null ? entity.getClassEntity().getName() : null,
                entity.getStream() != null ? entity.getStream().getId() : null,
                entity.getStream() != null ? entity.getStream().getName() : null,
                entity.getAcademicYear() != null ? entity.getAcademicYear().getId() : null,
                entity.getAcademicYear() != null ? entity.getAcademicYear().getYear() : null
        );
    }

    /** Maps teacher assignments to response DTOs. */
    public static List<AssignmentResponse> toResponseList(List<TeacherAssignment> entities) {
        return entities.stream()
                .map(TeacherAssignmentMapper::toResponse)
                .toList();
    }
}
