package com.musomi.manager.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musomi.manager.AbstractIntegrationTest;
import com.musomi.manager.entity.School;
import com.musomi.manager.entity.User;
import com.musomi.manager.entity.UserSession;
import com.musomi.manager.entity.enums.Role;
import com.musomi.manager.repository.UserRepository;
import com.musomi.manager.repository.UserSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(AuthIntegrationTest.JsonConfiguration.class)
class AuthIntegrationTest extends AbstractIntegrationTest {

    private static final String USERNAME = "integration-admin";
    private static final String PASSWORD = "test12345678";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @TestConfiguration(proxyBeanMethods = false)
    static class JsonConfiguration {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @BeforeEach
    void createTestUser() {
        Optional<User> existing = userRepository.findBySchoolIdAndUsername(1L, USERNAME);
        User user = existing.orElseGet(() -> User.builder()
                .school(School.builder().id(1L).build())
                .username(USERNAME)
                .build());
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setFullName("Integration Administrator");
        user.setRole(Role.ADMIN);
        user.setIsActive(true);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        User savedUser = userRepository.save(user);

        List<UserSession> staleSessions = userSessionRepository.findAll().stream()
                .filter(session -> session.getUser().getId().equals(savedUser.getId()))
                .toList();
        userSessionRepository.deleteAllInBatch(staleSessions);
    }

    @Test
    @DisplayName("should login and return token when credentials are valid")
    void shouldLoginAndReturnTokenWhenCredentialsValid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest(USERNAME, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.username").value(USERNAME));
    }

    @Test
    @DisplayName("should return 401 when password is wrong")
    void shouldReturn401WhenPasswordWrong() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest(USERNAME, "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("should return 401 when username is not found")
    void shouldReturn401WhenUsernameNotFound() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest("integration-user-missing", PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("should return user summary when me is called with a valid token")
    void shouldReturnUserSummaryWhenMeCalledWithValidToken() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(USERNAME));
    }

    @Test
    @DisplayName("should invalidate token after logout")
    void shouldInvalidateTokenAfterLogout() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("SESSION_EXPIRED"));
    }

    private String loginAndGetToken() throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest(USERNAME, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.path("data").path("token").asText();
    }

    private String loginRequest(String username, String password) throws Exception {
        return objectMapper.writeValueAsString(java.util.Map.of(
                "username", username,
                "password", password));
    }
}
