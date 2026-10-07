package com.musomi.manager.controller.api;

import java.util.List;

import com.musomi.manager.dto.request.GenerateReportRequest;
import com.musomi.manager.dto.response.ApiResponse;
import com.musomi.manager.dto.response.ReportResponse;
import com.musomi.manager.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Teacher and admin endpoints for reports. */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public class ReportApiController {

    private final ReportService reportService;

    /** Starts report generation for a class and term. */
    @PostMapping("/generate")
    public ApiResponse<List<ReportResponse>> generateReports(
            @Valid @RequestBody GenerateReportRequest request) {
        // TODO: Add class report generation to ReportService when PDF generation is implemented.
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Report generation is not available yet");
    }

    /** Lists generated reports for a student. */
    @GetMapping("/student/{studentId}")
    public ApiResponse<List<ReportResponse>> listByStudent(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long termId) {
        return ApiResponse.success(reportService.listByStudent(studentId));
    }

    /** Downloads a generated PDF report. */
    @GetMapping("/download/{reportId}")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long reportId) {
        // TODO: Load report metadata and stream the PDF once file generation is implemented.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .build();
    }
}
