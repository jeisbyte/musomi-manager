package com.musomi.manager.controller.api;

import com.musomi.manager.entity.ReportRequest;
import com.musomi.manager.dto.response.ApiResponse;
import com.musomi.manager.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Admin endpoints for report-generation requests. */
@RestController
@RequestMapping("/api/v1/admin/report-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportRequestApiController {

    private static final Long SCHOOL_ID = 1L;
    private static final Long ADMIN_ID = 1L;

    private final ReportService reportService;

    /** Creates a pending report request for a student and term. */
    @PostMapping
    public ApiResponse<ReportRequest> createReportRequest(
            @RequestParam Long studentId,
            @RequestParam Long termId) {
        return ApiResponse.success(
                reportService.createReportRequest(SCHOOL_ID, studentId, termId, ADMIN_ID));
    }

    /** Marks a report request as generated. */
    @PostMapping("/{requestId}/mark-generated")
    public ResponseEntity<Void> markGenerated(
            @PathVariable Long requestId,
            @RequestParam Long reportId) {
        reportService.markGenerated(requestId, reportId);
        return ResponseEntity.noContent().build();
    }
}
