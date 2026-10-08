package com.musomi.manager.controller.api;

import java.util.List;

import com.musomi.manager.dto.request.CreateStudentRequest;
import com.musomi.manager.dto.request.UpdateStudentRequest;
import com.musomi.manager.dto.response.ApiResponse;
import com.musomi.manager.dto.response.StudentResponse;
import com.musomi.manager.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Admin endpoints for managing students. */
@RestController
@RequestMapping("/api/v1/admin/students")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStudentApiController {

    private static final Long SCHOOL_ID = 1L;

    private final StudentService studentService;

    /** Lists students with optional class, stream, status, and search filters. */
    @GetMapping
    public ApiResponse<List<StudentResponse>> listStudents(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long streamId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        return ApiResponse.success(studentService.listStudents(SCHOOL_ID, classId, streamId, status, search));
    }

    /** Creates a student. */
    @PostMapping
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(
            @Valid @RequestBody CreateStudentRequest request) {
        StudentResponse created = studentService.createStudent(SCHOOL_ID, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    /** Returns a student with guardian details. */
    @GetMapping("/{id}")
    public ApiResponse<StudentResponse> getStudent(@PathVariable Long id) {
        return ApiResponse.success(studentService.getStudent(id));
    }

    /** Updates a student's profile. */
    @PutMapping("/{id}")
    public ApiResponse<StudentResponse> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudentRequest request) {
        return ApiResponse.success(studentService.updateStudent(id, request));
    }

    /** Deactivates a student without deleting the record. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateStudent(@PathVariable Long id) {
        studentService.deactivateStudent(id);
        return ResponseEntity.noContent().build();
    }
}
