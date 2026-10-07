package com.musomi.manager.mapper;

import java.util.List;

import com.musomi.manager.dto.response.StreamResponse;
import com.musomi.manager.entity.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Maps stream entities to response DTOs. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StreamMapper {

    /** Maps a stream to its response DTO. */
    public static StreamResponse toResponse(Stream stream) {
        if (stream == null) {
            return null;
        }
        return new StreamResponse(
                stream.getId(),
                stream.getClassEntity() != null ? stream.getClassEntity().getId() : null,
                stream.getName(),
                stream.getCapacity(),
                stream.getIsActive()
        );
    }

    /** Maps a list of streams to response DTOs. */
    public static List<StreamResponse> toResponseList(List<Stream> streams) {
        return streams.stream()
                .map(StreamMapper::toResponse)
                .toList();
    }
}
