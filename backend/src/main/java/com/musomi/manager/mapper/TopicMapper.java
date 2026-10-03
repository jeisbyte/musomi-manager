package com.musomi.manager.mapper;

import java.util.List;

import com.musomi.manager.dto.response.TopicResponse;
import com.musomi.manager.entity.Topic;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Maps topic entities to response DTOs. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TopicMapper {

    /** Maps a topic to its response DTO. */
    public static TopicResponse toResponse(Topic topic) {
        if (topic == null) {
            return null;
        }
        return new TopicResponse(
                topic.getId(),
                topic.getSubject() != null ? topic.getSubject().getId() : null,
                topic.getName(),
                topic.getDescription(),
                topic.getSortOrder()
        );
    }

    /** Maps a list of topics to response DTOs. */
    public static List<TopicResponse> toResponseList(List<Topic> topics) {
        return topics.stream()
                .map(TopicMapper::toResponse)
                .toList();
    }
}
