package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.EmployeeRequestDto;
import com.example.employeemanagement.entity.EmploymentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class EmployeeControllerSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Unauthenticated request to /api/employees must return HTTP 401 Unauthorized")
    void getEmployees_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @WithMockUser(username = "user@company.com", roles = {"USER"})
    @DisplayName("Authenticated USER role can read employees list")
    void getEmployees_AsUser_Returns200() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @WithMockUser(username = "user@company.com", roles = {"USER"})
    @DisplayName("Authenticated USER role cannot create employee - must return HTTP 403 Forbidden")
    void createEmployee_AsUser_Returns403() throws Exception {
        EmployeeRequestDto request = new EmployeeRequestDto(
                "Unauthorized Test",
                "unauth@company.com",
                1L,
                "Software Engineer",
                "+1-555-0999",
                LocalDate.now().minusMonths(1),
                EmploymentStatus.ACTIVE,
                new BigDecimal("95000.00")
        );

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @WithMockUser(username = "admin@company.com", roles = {"ADMIN"})
    @DisplayName("Authenticated ADMIN role can create employee - returns HTTP 201 Created")
    void createEmployee_AsAdmin_Returns201() throws Exception {
        EmployeeRequestDto request = new EmployeeRequestDto(
                "Samantha Ray",
                "samantha.ray" + System.currentTimeMillis() + "@company.com",
                1L,
                "Site Reliability Engineer",
                "+1-555-4321",
                LocalDate.now().minusDays(10),
                EmploymentStatus.ACTIVE,
                new BigDecimal("128000.00")
        );

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Samantha Ray"))
                .andExpect(jsonPath("$.data.departmentName").value("Engineering"))
                .andExpect(jsonPath("$.data.salary").value(128000.00));
    }

    @Test
    @WithMockUser(username = "admin@company.com", roles = {"ADMIN"})
    @DisplayName("Validation failure returns HTTP 400 with descriptive field errors")
    void createEmployee_ValidationFailure_Returns400() throws Exception {
        EmployeeRequestDto invalidRequest = new EmployeeRequestDto(
                "", // Blank name
                "invalid-email-address", // Invalid email
                null, // Missing department ID
                "", // Blank job title
                "", // Blank phone
                LocalDate.now().plusDays(5), // Future date
                null,
                new BigDecimal("-500.00") // Negative salary
        );

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details", hasSize(greaterThan(3))));
    }

    @Test
    @WithMockUser(username = "admin@company.com", roles = {"ADMIN"})
    @DisplayName("Duplicate email rejection returns HTTP 409 Conflict")
    void createEmployee_DuplicateEmail_Returns409() throws Exception {
        // Devon Vance email is seeded initially
        EmployeeRequestDto duplicateRequest = new EmployeeRequestDto(
                "Devon Clone",
                "devon.vance@company.com",
                1L,
                "Cloud Architect",
                "+1-555-9999",
                LocalDate.now().minusDays(5),
                EmploymentStatus.ACTIVE,
                new BigDecimal("145000.00")
        );

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("devon.vance@company.com")));
    }

    @Test
    @WithMockUser(username = "user@company.com", roles = {"USER"})
    @DisplayName("Dashboard summary endpoint returns 200 OK with aggregated statistics")
    void getDashboardSummary_Returns200() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalEmployees", greaterThan(0)))
                .andExpect(jsonPath("$.data.activeEmployees", greaterThan(0)))
                .andExpect(jsonPath("$.data.departmentDistribution").isArray());
    }

    @Test
    @WithMockUser(username = "user@company.com", roles = {"USER"})
    @DisplayName("AI natural language query endpoint returns 200 OK with answer")
    void queryAnalytics_Returns200() throws Exception {
        mockMvc.perform(post("/api/ai/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"How many active employees work in each department?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.answer").isNotEmpty())
                .andExpect(jsonPath("$.data.source").isString());
    }

    @Test
    @WithMockUser(username = "user@company.com", roles = {"USER"})
    @DisplayName("USER role attempting DELETE employee returns HTTP 403 Forbidden")
    void deleteEmployee_AsUser_Returns403() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isForbidden());
    }
}
