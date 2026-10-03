package com.musomi.manager.mapper;

import java.util.List;

import com.musomi.manager.dto.response.ClassResponse;
import com.musomi.manager.entity.ClassEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Maps class entities to response DTOs. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClassMapper {

    /** Maps a class to its response DTO. */
    public static ClassResponse toResponse(ClassEntity classEntity) {
        if (classEntity == null) {
            return null;
        }
        return new ClassResponse(
                classEntity.getId(),
                classEntity.getName(),
                classEntity.getClassLevel() != null ? classEntity.getClassLevel().getId() : null,
                classEntity.getAcademicYear() != null ? classEntity.getAcademicYear().getId() : null,
                classEntity.getClassTeacher() != null ? classEntity.getClassTeacher().getId() : null,
                classEntity.getIsActive(),
                classEntity.getCreatedAt()
        );
    }

    /** Maps a list of classes to response DTOs. */
    public static List<ClassResponse> toResponseList(List<ClassEntity> classes) {
        return classes.stream()
                .map(ClassMapper::toResponse)
                .toList();
    }
}
