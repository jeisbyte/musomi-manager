package com.musomi.manager.controller.api;

import java.util.List;

import com.musomi.manager.dto.request.CreateAcademicYearRequest;
import com.musomi.manager.dto.response.AcademicYearResponse;
import com.musomi.manager.dto.response.ApiResponse;
import com.musomi.manager.service.AcademicYearService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Admin endpoints for academic years. */
@RestController
@RequestMapping("/api/v1/admin/academic-years")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAcademicYearApiController {

    private static final Long SCHOOL_ID = 1L;

    private final AcademicYearService academicYearService;

    /** Lists academic years for the school. */
    @GetMapping
    public ApiResponse<List<AcademicYearResponse>> listYears() {
        return ApiResponse.success(academicYearService.listYears(SCHOOL_ID));
    }

    /** Creates an academic year. */
    @PostMapping
    public ResponseEntity<ApiResponse<AcademicYearResponse>> createYear(
            @Valid @RequestBody CreateAcademicYearRequest request) {
        AcademicYearResponse created = academicYearService.createYear(SCHOOL_ID, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    /** Sets the specified academic year as current. */
    @PutMapping("/{id}/current")
    public ApiResponse<AcademicYearResponse> setCurrent(@PathVariable Long id) {
        return ApiResponse.success(academicYearService.setCurrent(id, SCHOOL_ID));
    }
}
