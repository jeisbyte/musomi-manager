package com.musomi.manager.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

import com.musomi.manager.dto.response.ScoreResponse;
import com.musomi.manager.entity.Score;

/** Maps score entities to response DTOs. */
public final class ScoreMapper {

    private ScoreMapper() {
    }

    /** Maps a score to its response DTO with a supplied grade. */
    public static ScoreResponse toResponse(Score entity, String grade) {
        if (entity == null) {
            return null;
        }
        LocalDateTime savedAt = entity.getUpdatedAt() != null ? entity.getUpdatedAt() : entity.getEnteredAt();
        return new ScoreResponse(
                entity.getId(),
                entity.getStudent() != null ? entity.getStudent().getId() : null,
                entity.getScore(),
                entity.getFeedback(),
                grade,
                savedAt
        );
    }

    /** Maps a list of scores to response DTOs using a grade function. */
    public static List<ScoreResponse> toResponseList(List<Score> entities, Function<Score, String> gradeFn) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(entity -> ScoreMapper.toResponse(entity, gradeFn.apply(entity)))
                .toList();
    }
}
