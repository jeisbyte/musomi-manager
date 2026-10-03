package com.musomi.manager.mapper;

import java.util.List;

import com.musomi.manager.dto.response.ClassLevelResponse;
import com.musomi.manager.entity.ClassLevel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Maps class level entities to response DTOs. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClassLevelMapper {

    /** Maps a class level to its response DTO. */
    public static ClassLevelResponse toResponse(ClassLevel classLevel) {
        if (classLevel == null) {
            return null;
        }
        return new ClassLevelResponse(
                classLevel.getId(),
                classLevel.getName(),
                classLevel.getSortOrder(),
                classLevel.getIsActive()
        );
    }

    /** Maps a list of class levels to response DTOs. */
    public static List<ClassLevelResponse> toResponseList(List<ClassLevel> classLevels) {
        return classLevels.stream()
                .map(ClassLevelMapper::toResponse)
                .toList();
    }
}
