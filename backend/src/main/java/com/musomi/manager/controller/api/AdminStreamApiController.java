package com.musomi.manager.controller.api;

import java.util.List;

import com.musomi.manager.dto.request.CreateStreamRequest;
import com.musomi.manager.dto.response.ApiResponse;
import com.musomi.manager.dto.response.StreamResponse;
import com.musomi.manager.service.StreamService;
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

/** Admin endpoints for class streams. */
@RestController
@RequestMapping("/api/v1/admin/streams")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStreamApiController {

    private static final Long SCHOOL_ID = 1L;

    private final StreamService streamService;

    /** Lists streams within a class. */
    @GetMapping
    public ApiResponse<List<StreamResponse>> listStreams(@RequestParam Long classId) {
        return ApiResponse.success(streamService.listStreams(classId));
    }

    /** Creates a stream within a class. */
    @PostMapping
    public ResponseEntity<ApiResponse<StreamResponse>> createStream(
            @Valid @RequestBody CreateStreamRequest request) {
        StreamResponse created = streamService.createStream(SCHOOL_ID, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }
}
