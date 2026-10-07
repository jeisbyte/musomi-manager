package com.musomi.manager.controller.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

import com.musomi.manager.dto.request.GenerateReportRequest;
import com.musomi.manager.dto.response.ApiResponse;
import com.musomi.manager.dto.response.ReportResponse;
import com.musomi.manager.entity.GeneratedReport;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
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
        return ApiResponse.success(reportService.generateForClassStreamTerm(
                1L, request.classId(), request.streamId(), request.termId(), 1L));
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
        GeneratedReport report = reportService.getGeneratedReport(reportId);
        if (report.getFilePath() == null || report.getFilePath().isBlank()) {
            throw new ResourceNotFoundException(ErrorCode.REPORT_NOT_FOUND);
        }
        try {
            byte[] file = Files.readAllBytes(Path.of(report.getFilePath()));
            String studentId = report.getStudent() == null || report.getStudent().getId() == null
                    ? "unknown"
                    : report.getStudent().getId().toString();
            String termId = report.getTerm() == null || report.getTerm().getId() == null
                    ? "unknown"
                    : report.getTerm().getId().toString();
            String filename = "report-" + studentId + "-" + termId + ".pdf";
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment().filename(filename).build().toString())
                    .body(file);
        } catch (IOException | InvalidPathException exception) {
            throw new ResourceNotFoundException(ErrorCode.REPORT_NOT_FOUND);
        }
    }
}
