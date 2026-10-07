package com.musomi.manager.controller.api;

import java.util.List;

import com.musomi.manager.dto.request.CreateSubjectRequest;
import com.musomi.manager.dto.request.CreateTopicRequest;
import com.musomi.manager.dto.response.ApiResponse;
import com.musomi.manager.dto.response.SubjectResponse;
import com.musomi.manager.dto.response.TopicResponse;
import com.musomi.manager.service.SubjectService;
import com.musomi.manager.service.TopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Admin endpoints for subjects and their topics. */
@RestController
@RequestMapping("/api/v1/admin/subjects")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSubjectApiController {

    private static final Long SCHOOL_ID = 1L;

    private final SubjectService subjectService;
    private final TopicService topicService;

    /** Lists active subjects for the school. */
    @GetMapping
    public ApiResponse<List<SubjectResponse>> listSubjects() {
        return ApiResponse.success(subjectService.listSubjects(SCHOOL_ID));
    }

    /** Creates a subject. */
    @PostMapping
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(
            @Valid @RequestBody CreateSubjectRequest request) {
        SubjectResponse created = subjectService.createSubject(SCHOOL_ID, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    /** Lists topics belonging to a subject. */
    @GetMapping("/{id}/topics")
    public ApiResponse<List<TopicResponse>> listTopics(@PathVariable Long id) {
        return ApiResponse.success(topicService.listTopics(id));
    }

    /** Creates a topic within the specified subject. */
    @PostMapping("/{id}/topics")
    public ResponseEntity<ApiResponse<TopicResponse>> createTopic(
            @PathVariable Long id,
            @Valid @RequestBody CreateTopicRequest request) {
        CreateTopicRequest topicRequest = new CreateTopicRequest(
                id, request.name(), request.description(), request.sortOrder());
        TopicResponse created = topicService.createTopic(SCHOOL_ID, topicRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }
}
