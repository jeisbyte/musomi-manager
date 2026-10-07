package com.musomi.manager.controller.api;

import java.util.List;

import com.musomi.manager.dto.request.CreateAssessmentRequest;
import com.musomi.manager.dto.request.UpdateAssessmentRequest;
import com.musomi.manager.dto.response.ApiResponse;
import com.musomi.manager.dto.response.AssessmentResponse;
import com.musomi.manager.service.AssessmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Teacher endpoints for managing assessments. */
@RestController
@RequestMapping("/api/v1/teacher/assessments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherAssessmentApiController {

    private static final Long SCHOOL_ID = 1L;
    private static final Long TEACHER_ID = 1L;

    private final AssessmentService assessmentService;

    /** Lists the teacher's assessments with optional filters. */
    @GetMapping
    public ApiResponse<List<AssessmentResponse>> listAssessments(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long termId,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(
                assessmentService.listAssessments(TEACHER_ID, classId, subjectId, termId, status));
    }

    /** Creates an assessment draft. */
    @PostMapping
    public ResponseEntity<ApiResponse<AssessmentResponse>> createAssessment(
            @Valid @RequestBody CreateAssessmentRequest request) {
        AssessmentResponse created = assessmentService.createAssessment(SCHOOL_ID, TEACHER_ID, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    /** Returns an assessment by id. */
    @GetMapping("/{id}")
    public ApiResponse<AssessmentResponse> getAssessment(@PathVariable Long id) {
        return ApiResponse.success(assessmentService.getAssessment(id));
    }

    /** Updates an assessment draft. */
    @PutMapping("/{id}")
    public ApiResponse<AssessmentResponse> updateAssessment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAssessmentRequest request) {
        return ApiResponse.success(assessmentService.updateAssessment(id, request));
    }

    /** Deletes an assessment draft. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssessment(@PathVariable Long id) {
        assessmentService.deleteAssessment(id);
        return ResponseEntity.noContent().build();
    }

    /** Publishes an assessment and returns its updated representation. */
    @PostMapping("/{id}/publish")
    public ApiResponse<AssessmentResponse> publishAssessment(@PathVariable Long id) {
        assessmentService.publishAssessment(id, TEACHER_ID);
        return ApiResponse.success(assessmentService.getAssessment(id));
    }
}
