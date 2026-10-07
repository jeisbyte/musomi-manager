package com.musomi.manager.controller.api;

import java.util.List;

import com.musomi.manager.dto.request.SaveCommentRequest;
import com.musomi.manager.dto.response.ApiResponse;
import com.musomi.manager.dto.response.CommentResponse;
import com.musomi.manager.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Teacher endpoints for student comments. */
@RestController
@RequestMapping("/api/v1/teacher")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherCommentApiController {

    private static final Long SCHOOL_ID = 1L;
    private static final Long TEACHER_ID = 1L;

    private final CommentService commentService;

    /** Lists comments for a student, optionally filtered by term. */
    @GetMapping("/students/{studentId}/comments")
    public ApiResponse<List<CommentResponse>> listComments(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long termId) {
        return ApiResponse.success(commentService.listComments(studentId, termId));
    }

    /** Creates or updates a teacher comment. */
    @PostMapping("/comments")
    public ApiResponse<CommentResponse> saveComment(@Valid @RequestBody SaveCommentRequest request) {
        return ApiResponse.success(commentService.saveComment(SCHOOL_ID, TEACHER_ID, request));
    }

    /** Deletes a teacher comment. */
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
