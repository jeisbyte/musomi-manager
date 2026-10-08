package com.musomi.manager.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.musomi.manager.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "ADMIN")
@Import(StudentIntegrationTest.JsonConfiguration.class)
class StudentIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration(proxyBeanMethods = false)
    static class JsonConfiguration {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Test
    @DisplayName("should create and retrieve student")
    void shouldCreateAndRetrieveStudent() throws Exception {
        JsonNode created = createStudent(uniqueAdmission("A"), "Integration Test Student");
        long id = created.path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/admin/students/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Integration Test Student"));
    }

    @Test
    @DisplayName("should throw when admission number is duplicated")
    void shouldThrowWhenDuplicateAdmissionNumber() throws Exception {
        String admissionNumber = uniqueAdmission("B");
        createStudent(admissionNumber, "First Duplicate Test");

        mockMvc.perform(post("/api/v1/admin/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentRequest(admissionNumber, "Second Duplicate Test")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_ADMISSION_NUMBER"));
    }

    @Test
    @DisplayName("should update student details")
    void shouldUpdateStudentDetails() throws Exception {
        JsonNode created = createStudent(uniqueAdmission("C"), "Before Update");
        long id = created.path("data").path("id").asLong();
        String request = objectMapper.writeValueAsString(Map.of(
                "fullName", "After Update",
                "gender", "FEMALE",
                "currentClassId", 1,
                "currentStreamId", 1,
                "status", "ACTIVE"));

        mockMvc.perform(put("/api/v1/admin/students/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("After Update"));
    }

    @Test
    @DisplayName("should list students with class filter")
    void shouldListStudentsWithClassFilter() throws Exception {
        mockMvc.perform(get("/api/v1/admin/students")
                        .param("classId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    @DisplayName("should deactivate student")
    void shouldDeactivateStudent() throws Exception {
        JsonNode created = createStudent(uniqueAdmission("D"), "Student To Deactivate");
        long id = created.path("data").path("id").asLong();

        mockMvc.perform(delete("/api/v1/admin/students/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/admin/students/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isActive").value(false));
    }

    @Test
    @DisplayName("should return 404 when student is not found")
    void shouldReturn404WhenStudentNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/admin/students/999999"))
                .andExpect(status().isNotFound());
    }

    private JsonNode createStudent(String admissionNumber, String fullName) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/admin/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentRequest(admissionNumber, fullName)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(response.path("data").path("id").asLong()).isPositive();
        return response;
    }

    private String studentRequest(String admissionNumber, String fullName) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "admissionNumber", admissionNumber,
                "fullName", fullName,
                "gender", "FEMALE",
                "currentClassId", 1,
                "currentStreamId", 1));
    }

    private String uniqueAdmission(String suffix) {
        return "INT/" + System.currentTimeMillis() + "/" + suffix + "-"
                + UUID.randomUUID().toString().substring(0, 8);
    }
}
