package com.musomi.manager.controller.api;

import java.util.List;

import com.musomi.manager.dto.request.CreateClassRequest;
import com.musomi.manager.dto.response.ApiResponse;
import com.musomi.manager.dto.response.ClassResponse;
import com.musomi.manager.service.ClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Admin endpoints for classes. */
@RestController
@RequestMapping("/api/v1/admin/classes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminClassApiController {

    private static final Long SCHOOL_ID = 1L;

    private final ClassService classService;

    /** Lists classes, optionally filtered by academic year. */
    @GetMapping
    public ApiResponse<List<ClassResponse>> listClasses(
            @RequestParam(required = false) Long yearId) {
        return ApiResponse.success(classService.listClasses(SCHOOL_ID, yearId));
    }

    /** Creates a class. */
    @PostMapping
    public ResponseEntity<ApiResponse<ClassResponse>> createClass(
            @Valid @RequestBody CreateClassRequest request) {
        ClassResponse created = classService.createClass(SCHOOL_ID, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }
}
