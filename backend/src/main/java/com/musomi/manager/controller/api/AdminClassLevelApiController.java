package com.musomi.manager.controller.api;

import java.util.List;

import com.musomi.manager.dto.request.CreateClassLevelRequest;
import com.musomi.manager.dto.response.ApiResponse;
import com.musomi.manager.dto.response.ClassLevelResponse;
import com.musomi.manager.service.ClassLevelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Admin endpoints for class levels. */
@RestController
@RequestMapping("/api/v1/admin/class-levels")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminClassLevelApiController {

    private static final Long SCHOOL_ID = 1L;

    private final ClassLevelService classLevelService;

    /** Lists active class levels for the school. */
    @GetMapping
    public ApiResponse<List<ClassLevelResponse>> listLevels() {
        return ApiResponse.success(classLevelService.listLevels(SCHOOL_ID));
    }

    /** Creates a class level. */
    @PostMapping
    public ResponseEntity<ApiResponse<ClassLevelResponse>> createLevel(
            @Valid @RequestBody CreateClassLevelRequest request) {
        ClassLevelResponse created = classLevelService.createLevel(SCHOOL_ID, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }
}
