package com.musomi.manager.mapper;

import com.musomi.manager.dto.response.CommentResponse;
import com.musomi.manager.entity.Comment;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Maps comment entities to response DTOs. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentMapper {

    /** Maps a comment to its response DTO. */
    public static CommentResponse toResponse(Comment comment) {
        if (comment == null) {
            return null;
        }
        return new CommentResponse(
                comment.getId(),
                comment.getStudent() != null ? comment.getStudent().getId() : null,
                comment.getTerm() != null ? comment.getTerm().getId() : null,
                comment.getSubject() != null ? comment.getSubject().getId() : null,
                comment.getSubject() != null ? comment.getSubject().getName() : null,
                comment.getCommentType(),
                comment.getCommentText(),
                comment.getWrittenBy() != null ? comment.getWrittenBy().getId() : null,
                comment.getWrittenBy() != null ? comment.getWrittenBy().getFullName() : null,
                comment.getWrittenAt()
        );
    }
}
