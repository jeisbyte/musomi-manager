package com.musomi.manager.mapper;

import java.util.List;

import com.musomi.manager.dto.response.GuardianResponse;
import com.musomi.manager.dto.response.StudentResponse;
import com.musomi.manager.entity.Guardian;
import com.musomi.manager.entity.Student;

/** Maps student and guardian entities to response DTOs. */
public final class StudentMapper {

    private StudentMapper() {
    }

    /** Maps a student without loading guardian details. */
    public static StudentResponse toResponse(Student entity) {
        if (entity == null) {
            return null;
        }
        return new StudentResponse(
                entity.getId(),
                entity.getAdmissionNumber(),
                entity.getFullName(),
                entity.getGender(),
                entity.getDateOfBirth(),
                entity.getCurrentClass() != null ? entity.getCurrentClass().getId() : null,
                entity.getCurrentClass() != null ? entity.getCurrentClass().getName() : null,
                entity.getCurrentStream() != null ? entity.getCurrentStream().getId() : null,
                entity.getCurrentStream() != null ? entity.getCurrentStream().getName() : null,
                entity.getStatus(),
                entity.getIsActive(),
                entity.getCreatedAt(),
                List.of()
        );
    }

    /** Maps a student with its guardian details. */
    public static StudentResponse toResponseWithGuardians(Student entity, List<Guardian> guardians) {
        StudentResponse response = toResponse(entity);
        if (response == null) {
            return null;
        }
        List<GuardianResponse> guardianResponses = guardians == null
                ? List.of()
                : guardians.stream().map(StudentMapper::toGuardianResponse).toList();
        return new StudentResponse(
                response.id(),
                response.admissionNumber(),
                response.fullName(),
                response.gender(),
                response.dateOfBirth(),
                response.currentClassId(),
                response.currentClassName(),
                response.currentStreamId(),
                response.currentStreamName(),
                response.status(),
                response.isActive(),
                response.createdAt(),
                guardianResponses
        );
    }

    /** Maps a list of students without loading guardian details. */
    public static List<StudentResponse> toResponseList(List<Student> entities) {
        return entities.stream()
                .map(StudentMapper::toResponse)
                .toList();
    }

    /** Maps a guardian to its response DTO. */
    public static GuardianResponse toGuardianResponse(Guardian guardian) {
        if (guardian == null) {
            return null;
        }
        return new GuardianResponse(
                guardian.getId(),
                guardian.getName(),
                guardian.getRelationship(),
                guardian.getPhone(),
                guardian.getEmail(),
                guardian.getIsPrimary()
        );
    }
}
