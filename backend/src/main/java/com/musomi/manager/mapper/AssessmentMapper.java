package com.musomi.manager.mapper;

import java.util.List;

import com.musomi.manager.dto.response.AssessmentResponse;
import com.musomi.manager.entity.Assessment;
import com.musomi.manager.entity.AssessmentTopic;

/** Maps assessment entities to response DTOs. */
public final class AssessmentMapper {

    private AssessmentMapper() {
    }

    /** Maps an assessment to its response DTO. */
    public static AssessmentResponse toResponse(Assessment entity) {
        return toResponse(entity, List.of());
    }

    /** Maps an assessment and its topic associations to a response DTO. */
    public static AssessmentResponse toResponse(Assessment entity, List<AssessmentTopic> assessmentTopics) {
        if (entity == null) {
            return null;
        }

        List<AssessmentResponse.TopicSummary> topics = assessmentTopics == null
                ? List.of()
                : assessmentTopics.stream()
                    .map(assessmentTopic -> new AssessmentResponse.TopicSummary(
                            assessmentTopic.getTopic().getId(),
                            assessmentTopic.getTopic().getName()))
                    .toList();

        return new AssessmentResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getType(),
                entity.getAssessmentDate(),
                entity.getMaxScore(),
                entity.getStatus(),
                entity.getClassEntity() != null ? entity.getClassEntity().getId() : null,
                entity.getClassEntity() != null ? entity.getClassEntity().getName() : null,
                entity.getStream() != null ? entity.getStream().getId() : null,
                entity.getStream() != null ? entity.getStream().getName() : null,
                entity.getSubject() != null ? entity.getSubject().getId() : null,
                entity.getSubject() != null ? entity.getSubject().getName() : null,
                entity.getTerm() != null ? entity.getTerm().getId() : null,
                entity.getTerm() != null ? entity.getTerm().getName() : null,
                entity.getTerm() != null && entity.getTerm().getAcademicYear() != null
                        ? entity.getTerm().getAcademicYear().getYear()
                        : null,
                entity.getTeacher() != null ? entity.getTeacher().getId() : null,
                entity.getTeacher() != null ? entity.getTeacher().getFullName() : null,
                topics,
                null,
                null,
                entity.getCreatedAt(),
                entity.getPublishedAt()
        );
    }

    /** Maps a list of assessments to response DTOs. */
    public static List<AssessmentResponse> toResponseList(List<Assessment> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(AssessmentMapper::toResponse)
                .toList();
    }
}
