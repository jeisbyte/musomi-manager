package com.musomi.manager.controller.api;

import java.util.List;

import com.musomi.manager.dto.request.CreateAssignmentRequest;
import com.musomi.manager.dto.response.ApiResponse;
import com.musomi.manager.dto.response.AssignmentResponse;
import com.musomi.manager.service.TeacherAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Admin endpoints for teacher assignments. */
@RestController
@RequestMapping("/api/v1/admin/assignments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAssignmentApiController {

    private static final Long SCHOOL_ID = 1L;

    private final TeacherAssignmentService teacherAssignmentService;

    /** Lists assignments for the school, optionally filtered by teacher. */
    @GetMapping
    public ApiResponse<List<AssignmentResponse>> listAssignments(
            @RequestParam(required = false) Long teacherId) {
        return ApiResponse.success(teacherAssignmentService.listAssignments(SCHOOL_ID, teacherId));
    }

    /** Creates a teacher assignment. */
    @PostMapping
    public ResponseEntity<ApiResponse<AssignmentResponse>> createAssignment(
            @Valid @RequestBody CreateAssignmentRequest request) {
        AssignmentResponse created = teacherAssignmentService.createAssignment(SCHOOL_ID, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    /** Deletes a teacher assignment. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        teacherAssignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }
}
