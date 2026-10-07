package com.musomi.manager.controller.api;

import com.musomi.manager.dto.response.ApiResponse;
import com.musomi.manager.dto.response.ImportConfirmResponse;
import com.musomi.manager.dto.response.ImportPreviewResponse;
import com.musomi.manager.service.StudentImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Admin endpoints for importing students from Excel files. */
@RestController
@RequestMapping("/api/v1/admin/students/import")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStudentImportApiController {

    private static final Long SCHOOL_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final StudentImportService studentImportService;

    /** Uploads a student spreadsheet and returns a validation preview. */
    @PostMapping
    public ApiResponse<ImportPreviewResponse> previewImport(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(studentImportService.previewImport(SCHOOL_ID, USER_ID, file));
    }

    /** Confirms a validated student import. */
    @PostMapping("/{importId}/confirm")
    public ApiResponse<ImportConfirmResponse> confirmImport(@PathVariable String importId) {
        return ApiResponse.success(studentImportService.confirmImport(SCHOOL_ID, importId));
    }

    /** Downloads the student import spreadsheet template. */
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX_CONTENT_TYPE))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("students-template.xlsx").build().toString())
                .body(studentImportService.buildTemplate());
    }
}
