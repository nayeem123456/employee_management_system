package com.example.employeemanagement.ai;

import com.example.employeemanagement.dto.AiQueryResponseDto;
import com.example.employeemanagement.dto.AiReportRequestDto;
import com.example.employeemanagement.dto.AiReportResponseDto;
import com.example.employeemanagement.entity.AiReportHistory;
import com.example.employeemanagement.entity.EmploymentStatus;
import com.example.employeemanagement.repository.AiReportHistoryRepository;
import com.example.employeemanagement.repository.EmployeeRepository;
import com.example.employeemanagement.service.impl.AiServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AiReportHistoryRepository aiReportHistoryRepository;

    @Mock
    private AiClient aiClient;

    private final RuleBasedFallbackEngine fallbackEngine = new RuleBasedFallbackEngine();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private AiServiceImpl aiService;

    @BeforeEach
    void setUp() {
        aiService = new AiServiceImpl(employeeRepository, aiReportHistoryRepository, aiClient, fallbackEngine, objectMapper);
        when(employeeRepository.count()).thenReturn(20L);
        when(employeeRepository.countByEmploymentStatus(EmploymentStatus.ACTIVE)).thenReturn(17L);
        when(employeeRepository.countByEmploymentStatus(EmploymentStatus.ON_LEAVE)).thenReturn(1L);
        when(employeeRepository.countByEmploymentStatus(EmploymentStatus.INACTIVE)).thenReturn(2L);
        Object[] deptRow = new Object[]{1L, "Engineering", "ENG", 8L, 7L, 110000.0};
        when(employeeRepository.getDepartmentAggregationStats()).thenReturn(java.util.Collections.singletonList(deptRow));
    }

    @Test
    @DisplayName("Should use AI Client when configured and successful")
    void queryAnalytics_AiClientConfigured_Success() {
        when(aiClient.isConfigured()).thenReturn(true);
        when(aiClient.generateText(anyString(), anyString())).thenReturn("There are 7 active engineers.");

        AiQueryResponseDto response = aiService.queryAnalytics("How many active engineers?");

        assertThat(response).isNotNull();
        assertThat(response.getSource()).isEqualTo("AI_LLM");
        assertThat(response.getAnswer()).isEqualTo("There are 7 active engineers.");
    }

    @Test
    @DisplayName("Should seamlessly trigger Rule-Based Fallback when AI Client throws an exception")
    void queryAnalytics_AiClientFails_FallbackTriggered() {
        when(aiClient.isConfigured()).thenReturn(true);
        when(aiClient.generateText(anyString(), anyString())).thenThrow(new RuntimeException("Connection timeout"));

        AiQueryResponseDto response = aiService.queryAnalytics("Which departments have the largest headcount?");

        assertThat(response).isNotNull();
        assertThat(response.getSource()).isEqualTo("RULE_BASED");
        assertThat(response.getAnswer()).contains("Engineering (ENG)");
        assertThat(response.getAnswer()).contains("8 employees");
    }

    @Test
    @DisplayName("Should use Rule-Based Fallback when AI Client is not configured")
    void queryAnalytics_AiClientNotConfigured_RuleBasedFallback() {
        when(aiClient.isConfigured()).thenReturn(false);

        AiQueryResponseDto response = aiService.queryAnalytics("Summarize employee distribution");

        assertThat(response).isNotNull();
        assertThat(response.getSource()).isEqualTo("RULE_BASED");
        assertThat(response.getAnswer()).contains("Workforce Distribution Summary");
        assertThat(response.getAnswer()).contains("20 total personnel");
    }

    @Test
    @DisplayName("Should generate structured report and persist audit history")
    void generateReport_Success() {
        when(aiClient.isConfigured()).thenReturn(false);
        when(employeeRepository.countByJoiningDateBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(3L);

        AiReportHistory mockHistory = new AiReportHistory();
        mockHistory.setId(101L);
        mockHistory.setTitle("Workforce Intelligence Report - ALL");
        when(aiReportHistoryRepository.save(any(AiReportHistory.class))).thenReturn(mockHistory);

        AiReportRequestDto requestDto = new AiReportRequestDto(LocalDate.now().minusMonths(3), LocalDate.now(), "ALL");
        AiReportResponseDto report = aiService.generateReport(requestDto);

        assertThat(report).isNotNull();
        assertThat(report.getReportId()).isEqualTo(101L);
        assertThat(report.getSource()).isEqualTo("RULE_BASED");
        assertThat(report.getExecutiveSummary()).isNotBlank();
        assertThat(report.getObservedTrends()).isNotEmpty();
        assertThat(report.getActionableRecommendations()).isNotEmpty();

        verify(aiReportHistoryRepository).save(any(AiReportHistory.class));
    }
}
