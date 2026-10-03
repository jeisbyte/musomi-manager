package com.musomi.manager.controller.api;

import com.musomi.manager.config.SecurityConfig;
import com.musomi.manager.dto.request.CreateUserRequest;
import com.musomi.manager.dto.request.UpdateUserRequest;
import com.musomi.manager.dto.response.PageResult;
import com.musomi.manager.dto.response.ResetPasswordResponse;
import com.musomi.manager.dto.response.UserResponse;
import com.musomi.manager.entity.UserSession;
import com.musomi.manager.entity.enums.Role;
import com.musomi.manager.exception.ErrorCode;
import com.musomi.manager.exception.ResourceNotFoundException;
import com.musomi.manager.exception.ValidationException;
import com.musomi.manager.repository.UserSessionRepository;
import com.musomi.manager.security.JwtTokenProvider;
import com.musomi.manager.service.UserService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserApiController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, AdminUserApiControllerTest.MethodSecurityTestConfiguration.class})
class AdminUserApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserSessionRepository userSessionRepository;

    @Test
    @DisplayName("should return paged users when admin calls list")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnPagedUsersWhenAdminCallsList() throws Exception {
        PageResult<UserResponse> users = new PageResult<>(
                List.of(user(1L, "teacher1"), user(2L, "teacher2")),
                0,
                20,
                2,
                1);
        when(userService.listUsers(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(users);

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data.length()").value(2))
                .andExpect(jsonPath("$.data.data[0].username").value("teacher1"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(1));
    }

    @Test
    @DisplayName("should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should return 403 when teacher calls list")
    @WithMockUser(roles = "TEACHER")
    void shouldReturn403WhenTeacherCallsList() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should create user and return 201")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateUserAndReturn201() throws Exception {
        CreateUserRequest request = createUserRequest();
        when(userService.createUser(null, request)).thenReturn(user(3L, "newuser"));

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value("newuser"));
    }

    @Test
    @DisplayName("should return 400 when create request is invalid")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400WhenCreateRequestIsInvalid() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                " ",
                "short",
                "New User",
                null,
                null,
                Role.TEACHER);

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));

        verify(userService, never()).createUser(any(), any());
    }

    @Test
    @DisplayName("should return 409 when username is taken")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn409WhenUsernameTaken() throws Exception {
        CreateUserRequest request = createUserRequest();
        when(userService.createUser(null, request))
                .thenThrow(new ValidationException(ErrorCode.USERNAME_TAKEN));

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("USERNAME_TAKEN"));
    }

    @Test
    @DisplayName("should return 404 when user is not found")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenUserNotFound() throws Exception {
        when(userService.getUser(99L)).thenThrow(new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        mockMvc.perform(get("/api/v1/admin/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("USER_NOT_FOUND"));
    }

    @Test
    @DisplayName("should return user when admin requests an existing user")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnUserWhenAdminRequestsExistingUser() throws Exception {
        when(userService.getUser(5L)).thenReturn(user(5L, "teacher1"));

        mockMvc.perform(get("/api/v1/admin/users/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.username").value("teacher1"));
    }

    @Test
    @DisplayName("should update user when admin submits a valid request")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateUserWhenAdminSubmitsValidRequest() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("Updated Name", "updated@school.ug", "+256700000009");
        when(userService.updateUser(5L, request)).thenReturn(user(5L, "teacher1"));

        mockMvc.perform(put("/api/v1/admin/users/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.username").value("teacher1"));
    }

    @Test
    @DisplayName("should return 204 when deleting user")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn204WhenDeletingUser() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/users/5")
                        .with(adminAuthentication(5L)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(userService).deactivateUser(5L, 5L);
    }

    @Test
    @DisplayName("should return 422 when deactivating self")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn422WhenDeactivatingSelf() throws Exception {
        doThrow(new ValidationException(ErrorCode.CANNOT_DEACTIVATE_SELF))
                .when(userService).deactivateUser(1L, 1L);

        mockMvc.perform(delete("/api/v1/admin/users/1")
                        .with(adminAuthentication(1L)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("CANNOT_DEACTIVATE_SELF"));
    }

    @Test
    @DisplayName("should return temporary password on reset")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnTemporaryPasswordOnReset() throws Exception {
        when(userService.resetPassword(5L)).thenReturn(new ResetPasswordResponse("Temp9x4Kp2"));

        mockMvc.perform(post("/api/v1/admin/users/5/reset-password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.temporaryPassword").value("Temp9x4Kp2"));
    }

    private static CreateUserRequest createUserRequest() {
        return new CreateUserRequest(
                "newuser",
                "password123",
                "New User",
                "new@school.ug",
                "+256700000001",
                Role.TEACHER);
    }

    private static UserResponse user(Long id, String username) {
        return new UserResponse(
                id,
                username,
                "Test User",
                "test@school.ug",
                "+256700000000",
                "TEACHER",
                true,
                null,
                null);
    }

    private static RequestPostProcessor adminAuthentication(Long userId) {
        return authentication(new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableMethodSecurity
    static class MethodSecurityTestConfiguration {
    }
}
