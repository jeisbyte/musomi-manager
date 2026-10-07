package com.musomi.manager.controller.api;

import java.util.List;

import com.musomi.manager.dto.request.SaveMarksBatchRequest;
import com.musomi.manager.dto.request.SaveMarksRequest;
import com.musomi.manager.dto.response.ApiResponse;
import com.musomi.manager.dto.response.MarkGridResponse;
import com.musomi.manager.dto.response.ScoreResponse;
import com.musomi.manager.service.ScoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Teacher endpoints for entering assessment marks. */
@RestController
@RequestMapping("/api/v1/teacher")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherMarkApiController {

    private static final Long TEACHER_ID = 1L;

    private final ScoreService scoreService;

    /** Loads the mark grid for an assessment. */
    @GetMapping("/assessments/{id}/grid")
    public ApiResponse<MarkGridResponse> getGrid(@PathVariable Long id) {
        return ApiResponse.success(scoreService.getGrid(id));
    }

    /** Updates one assessment score. */
    @PutMapping("/scores/{scoreId}")
    public ApiResponse<ScoreResponse> updateScore(
            @PathVariable Long scoreId,
            @Valid @RequestBody SaveMarksRequest request) {
        return ApiResponse.success(scoreService.updateScore(scoreId, request, TEACHER_ID));
    }

    /** Saves a batch of scores for an assessment. */
    @PostMapping("/assessments/{id}/scores/batch")
    public ApiResponse<List<ScoreResponse>> saveBatch(
            @PathVariable Long id,
            @Valid @RequestBody SaveMarksBatchRequest request) {
        return ApiResponse.success(scoreService.saveBatch(id, request, TEACHER_ID));
    }
}
