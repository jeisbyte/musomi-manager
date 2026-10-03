package com.musomi.manager.controller.api;

import java.util.List;

import com.musomi.manager.dto.request.CreateTermRequest;
import com.musomi.manager.dto.response.ApiResponse;
import com.musomi.manager.dto.response.TermResponse;
import com.musomi.manager.service.TermService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Admin endpoints for school terms. */
@RestController
@RequestMapping("/api/v1/admin/terms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTermApiController {

    private static final Long SCHOOL_ID = 1L;

    private final TermService termService;

    /** Lists school terms, optionally filtered by academic year. */
    @GetMapping
    public ApiResponse<List<TermResponse>> listTerms(
            @RequestParam(required = false) Long yearId) {
        return ApiResponse.success(termService.listTerms(SCHOOL_ID, yearId));
    }

    /** Creates a school term. */
    @PostMapping
    public ResponseEntity<ApiResponse<TermResponse>> createTerm(
            @Valid @RequestBody CreateTermRequest request) {
        TermResponse created = termService.createTerm(SCHOOL_ID, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    /** Sets the specified term as current. */
    @PutMapping("/{id}/current")
    public ApiResponse<TermResponse> setCurrent(@PathVariable Long id) {
        return ApiResponse.success(termService.setCurrent(id, SCHOOL_ID));
    }
}
