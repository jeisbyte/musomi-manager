package com.musomi.manager.controller.api;

import com.musomi.manager.dto.request.LoginRequest;
import com.musomi.manager.dto.response.ApiResponse;
import com.musomi.manager.dto.response.LoginResponse;
import com.musomi.manager.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> details = new HashMap<>();
        if (auth != null) {
            details.put("userId", auth.getPrincipal());
            details.put("schoolId", auth.getDetails());
            details.put("authorities", auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList());
        }
        return ApiResponse.success(details);
    }

    @PostMapping("/logout")
    public ApiResponse<Map<String, String>> logout() {
        SecurityContextHolder.clearContext();
        return ApiResponse.success(Map.of("message", "Logged out"));
    }
}
