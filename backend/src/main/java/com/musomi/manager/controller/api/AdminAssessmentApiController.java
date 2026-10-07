package com.musomi.manager.controller.api;

import java.util.List;

import com.musomi.manager.dto.response.ApiResponse;
import com.musomi.manager.dto.response.AssessmentResponse;
import com.musomi.manager.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Admin endpoint for listing assessments. */
@RestController
@RequestMapping("/api/v1/admin/assessments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAssessmentApiController {

    private static final Long TEACHER_ID = 1L;

    private final AssessmentService assessmentService;

    /** Lists assessments with optional class, subject, and term filters. */
    @GetMapping
    public ApiResponse<List<AssessmentResponse>> listAssessments(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long termId) {
        return ApiResponse.success(
                assessmentService.listAssessments(TEACHER_ID, classId, subjectId, termId, null));
    }
}
