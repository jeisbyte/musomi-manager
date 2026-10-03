package com.musomi.manager.controller.api;

import com.musomi.manager.dto.request.CreateUserRequest;
import com.musomi.manager.dto.request.UpdateUserRequest;
import com.musomi.manager.dto.response.ApiResponse;
import com.musomi.manager.dto.response.PageResult;
import com.musomi.manager.dto.response.ResetPasswordResponse;
import com.musomi.manager.dto.response.UserResponse;
import com.musomi.manager.entity.enums.Role;
import com.musomi.manager.exception.AuthenticationException;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserApiController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<PageResult<UserResponse>> listUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ApiResponse.success(
                userService.listUsers(null, role, active, search, pageable));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        // TODO(backend): audit_log when V023 lands
        UserResponse created = userService.createUser(null, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(@PathVariable Long id) {
        return ApiResponse.success(userService.getUser(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        // TODO(backend): audit_log when V023 lands
        return ApiResponse.success(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        // TODO(backend): audit_log when V023 lands
        userService.deactivateUser(id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reset-password")
    public ApiResponse<ResetPasswordResponse> resetPassword(@PathVariable Long id) {
        // TODO(backend): audit_log when V023 lands
        return ApiResponse.success(userService.resetPassword(id));
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof Long userId)) {
            throw new AuthenticationException(ErrorCode.SESSION_EXPIRED);
        }
        return userId;
    }
}