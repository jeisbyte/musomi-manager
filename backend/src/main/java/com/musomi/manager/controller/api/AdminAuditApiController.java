package com.musomi.manager.controller.api;

import java.util.List;

import com.musomi.manager.dto.response.ApiResponse;
import com.musomi.manager.dto.response.AuditLogResponse;
import com.musomi.manager.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Admin endpoint for querying audit events. */
@RestController
@RequestMapping("/api/v1/admin/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditApiController {

    private static final Long SCHOOL_ID = 1L;

    private final AuditService auditService;

    /** Lists audit events filtered by user, action, entity, or the default school. */
    @GetMapping
    public ApiResponse<List<AuditLogResponse>> listAudit(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId) {
        if (userId != null) {
            return ApiResponse.success(auditService.listByUser(userId));
        }
        if (action != null) {
            return ApiResponse.success(auditService.listByAction(action));
        }
        if (entityType != null && entityId != null) {
            return ApiResponse.success(auditService.listByEntity(entityType, entityId));
        }
        return ApiResponse.success(auditService.listBySchool(SCHOOL_ID));
    }
}
