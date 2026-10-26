package com.example.employeemanagement.service.impl;

import com.example.employeemanagement.ai.AiClient;
import com.example.employeemanagement.ai.RuleBasedFallbackEngine;
import com.example.employeemanagement.dto.AiQueryResponseDto;
import com.example.employeemanagement.dto.AiReportRequestDto;
import com.example.employeemanagement.dto.AiReportResponseDto;
import com.example.employeemanagement.dto.DepartmentStatDto;
import com.example.employeemanagement.entity.AiReportHistory;
import com.example.employeemanagement.entity.EmploymentStatus;
import com.example.employeemanagement.repository.AiReportHistoryRepository;
import com.example.employeemanagement.repository.EmployeeRepository;
import com.example.employeemanagement.security.SecurityUtils;
import com.example.employeemanagement.service.AiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class AiServiceImpl implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiServiceImpl.class);

    private final EmployeeRepository employeeRepository;
    private final AiReportHistoryRepository aiReportHistoryRepository;
    private final AiClient aiClient;
    private final RuleBasedFallbackEngine fallbackEngine;
    private final ObjectMapper objectMapper;

    public AiServiceImpl(EmployeeRepository employeeRepository,
                         AiReportHistoryRepository aiReportHistoryRepository,
                         AiClient aiClient,
                         RuleBasedFallbackEngine fallbackEngine,
                         ObjectMapper objectMapper) {
        this.employeeRepository = employeeRepository;
        this.aiReportHistoryRepository = aiReportHistoryRepository;
        this.aiClient = aiClient;
        this.fallbackEngine = fallbackEngine;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public AiQueryResponseDto queryAnalytics(String query) {
        String sanitizedQuery = query != null ? query.trim() : "";
        log.info("Processing natural-language analytics query: '{}'", sanitizedQuery);

        long total = employeeRepository.count();
        long active = employeeRepository.countByEmploymentStatus(EmploymentStatus.ACTIVE);
        long onLeave = employeeRepository.countByEmploymentStatus(EmploymentStatus.ON_LEAVE);
        long inactive = employeeRepository.countByEmploymentStatus(EmploymentStatus.INACTIVE);

        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        long newJoiners = employeeRepository.countByJoiningDateBetween(thirtyDaysAgo, LocalDate.now());

        List<DepartmentStatDto> departmentStats = getDepartmentStats(total);

        Map<String, Object> facts = new LinkedHashMap<>();
        facts.put("totalEmployees", total);
        facts.put("activeEmployees", active);
        facts.put("onLeaveEmployees", onLeave);
        facts.put("inactiveEmployees", inactive);
        facts.put("departmentsCount", departmentStats.size());
        facts.put("newJoinersLast30Days", newJoiners);

        String answer;
        String source;

        if (aiClient.isConfigured()) {
            try {
                String systemInstruction = "You are an enterprise workforce analytics AI assistant. " +
                        "Answer the user's question using ONLY the provided verified metrics facts. " +
                        "Never fabricate or guess metrics. Never generate SQL. Do not disclose personal employee credentials or passwords. " +
                        "Provide a concise, professional markdown answer with bullet points if helpful.";

                StringBuilder factPrompt = new StringBuilder();
                factPrompt.append("FACTS:\n");
                factPrompt.append(String.format("- Total employees: %d\n", total));
                factPrompt.append(String.format("- Active: %d, On Leave: %d, Inactive: %d\n", active, onLeave, inactive));
                factPrompt.append(String.format("- New joiners in last 30 days: %d\n", newJoiners));
                factPrompt.append("- Department Breakdown:\n");
                for (DepartmentStatDto d : departmentStats) {
                    factPrompt.append(String.format("  * %s (%s): %d total, %d active (%.1f%% of company)\n",
                            d.getDepartmentName(), d.getDepartmentCode(), d.getTotalHeadcount(), d.getActiveHeadcount(), d.getPercentage()));
                }
                factPrompt.append("\nUSER QUESTION: ").append(sanitizedQuery);

                answer = aiClient.generateText(factPrompt.toString(), systemInstruction);
                source = "AI_LLM";
            } catch (Exception ex) {
                log.warn("AI generation encountered error, seamlessly invoking rule-based fallback: {}", ex.getMessage());
                answer = fallbackEngine.answerQuery(sanitizedQuery, total, active, onLeave, inactive, departmentStats, newJoiners);
                source = "RULE_BASED";
            }
        } else {
            answer = fallbackEngine.answerQuery(sanitizedQuery, total, active, onLeave, inactive, departmentStats, newJoiners);
            source = "RULE_BASED";
        }

        return new AiQueryResponseDto(sanitizedQuery, answer, source, facts);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getWorkforceInsights() {
        long total = employeeRepository.count();
        long active = employeeRepository.countByEmploymentStatus(EmploymentStatus.ACTIVE);
        long onLeave = employeeRepository.countByEmploymentStatus(EmploymentStatus.ON_LEAVE);
        long inactive = employeeRepository.countByEmploymentStatus(EmploymentStatus.INACTIVE);

        LocalDate ninetyDaysAgo = LocalDate.now().minusDays(90);
        long newJoiners = employeeRepository.countByJoiningDateBetween(ninetyDaysAgo, LocalDate.now());

        List<DepartmentStatDto> departmentStats = getDepartmentStats(total);

        List<String> trends = fallbackEngine.generateTrends(total, active, onLeave, inactive, departmentStats, newJoiners);
        List<String> recommendations = fallbackEngine.generateRecommendations(total, active, onLeave, inactive, departmentStats, newJoiners);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalEmployees", total);
        result.put("activeEmployees", active);
        result.put("onLeaveEmployees", onLeave);
        result.put("inactiveEmployees", inactive);
        result.put("newJoinersLastQuarter", newJoiners);
        result.put("departmentStats", departmentStats);
        result.put("trends", trends);
        result.put("recommendations", recommendations);
        result.put("isAiEnabled", aiClient.isConfigured());

        return result;
    }

    @Override
    @Transactional
    public AiReportResponseDto generateReport(AiReportRequestDto requestDto) {
        LocalDate fromDate = requestDto.getDateFrom() != null ? requestDto.getDateFrom() : LocalDate.now().minusMonths(6);
        LocalDate toDate = requestDto.getDateTo() != null ? requestDto.getDateTo() : LocalDate.now();
        String focus = requestDto.getFocusArea() != null ? requestDto.getFocusArea() : "ALL";

        long total = employeeRepository.count();
        long active = employeeRepository.countByEmploymentStatus(EmploymentStatus.ACTIVE);
        long onLeave = employeeRepository.countByEmploymentStatus(EmploymentStatus.ON_LEAVE);
        long inactive = employeeRepository.countByEmploymentStatus(EmploymentStatus.INACTIVE);
        long periodJoiners = employeeRepository.countByJoiningDateBetween(fromDate, toDate);

        List<DepartmentStatDto> departmentStats = getDepartmentStats(total);

        String summary;
        String source;

        if (aiClient.isConfigured()) {
            try {
                String systemInstruction = "You are a senior workforce intelligence consultant. " +
                        "Generate a formal, high-impact executive summary for a workforce report based strictly on the provided factual metrics. " +
                        "Do not speculate on health, personality, or non-factual attributes. Be concise and authoritative.";

                String userPrompt = String.format(
                        "Generate an executive report summary for period %s to %s with focus '%s'.\n" +
                        "Facts:\n- Total Headcount: %d\n- Active: %d (%.1f%%)\n- On Leave: %d\n- Inactive: %d\n- New Joiners in Period: %d\n" +
                        "- Departments: %s",
                        fromDate, toDate, focus, total, active,
                        total > 0 ? ((double) active / total * 100) : 0, onLeave, inactive, periodJoiners,
                        departmentStats.stream().map(d -> d.getDepartmentName() + " (" + d.getTotalHeadcount() + ")").toList()
                );

                summary = aiClient.generateText(userPrompt, systemInstruction);
                source = "AI_LLM";
            } catch (Exception ex) {
                log.warn("AI generation failed for report, falling back: {}", ex.getMessage());
                summary = fallbackEngine.generateExecutiveReportSummary(fromDate, toDate, total, active, onLeave, inactive, departmentStats, periodJoiners, focus);
                source = "RULE_BASED";
            }
        } else {
            summary = fallbackEngine.generateExecutiveReportSummary(fromDate, toDate, total, active, onLeave, inactive, departmentStats, periodJoiners, focus);
            source = "RULE_BASED";
        }

        List<String> trends = fallbackEngine.generateTrends(total, active, onLeave, inactive, departmentStats, periodJoiners);
        List<String> recommendations = fallbackEngine.generateRecommendations(total, active, onLeave, inactive, departmentStats, periodJoiners);

        Map<String, Object> keyMetrics = new LinkedHashMap<>();
        keyMetrics.put("totalEmployees", total);
        keyMetrics.put("activeEmployees", active);
        keyMetrics.put("onLeaveEmployees", onLeave);
        keyMetrics.put("inactiveEmployees", inactive);
        keyMetrics.put("activeRate", total > 0 ? String.format("%.1f%%", (double) active / total * 100) : "0%");
        keyMetrics.put("periodNewJoiners", periodJoiners);
        keyMetrics.put("evaluatedPeriod", fromDate + " to " + toDate);

        String metricsJson = "";
        try {
            metricsJson = objectMapper.writeValueAsString(keyMetrics);
        } catch (Exception ignored) {
        }

        AiReportHistory history = new AiReportHistory(
                "Workforce Intelligence Report - " + focus,
                fromDate,
                toDate,
                SecurityUtils.getCurrentUsername(),
                source,
                summary,
                metricsJson
        );
        AiReportHistory saved = aiReportHistoryRepository.save(history);

        AiReportResponseDto response = new AiReportResponseDto();
        response.setReportId(saved.getId());
        response.setTitle(saved.getTitle());
        response.setDateFrom(fromDate);
        response.setDateTo(toDate);
        response.setExecutiveSummary(summary);
        response.setKeyMetrics(keyMetrics);
        response.setDepartmentBreakdown(departmentStats);
        response.setObservedTrends(trends);
        response.setActionableRecommendations(recommendations);
        response.setSource(source);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiReportHistory> getReportHistory() {
        return aiReportHistoryRepository.findTop10ByOrderByCreatedAtDesc();
    }

    private List<DepartmentStatDto> getDepartmentStats(long total) {
        List<DepartmentStatDto> departmentStats = new ArrayList<>();
        List<Object[]> rawStats = employeeRepository.getDepartmentAggregationStats();
        boolean isAdmin = SecurityUtils.isAdmin();

        for (Object[] row : rawStats) {
            Long deptId = ((Number) row[0]).longValue();
            String deptName = (String) row[1];
            String deptCode = (String) row[2];
            long count = ((Number) row[3]).longValue();
            long activeCount = ((Number) row[4]).longValue();
            Double avgSalaryNum = row[5] != null ? ((Number) row[5]).doubleValue() : null;

            double deptPercentage = total > 0
                    ? BigDecimal.valueOf((double) count / total * 100).setScale(1, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;

            BigDecimal avgSalary = (isAdmin && avgSalaryNum != null)
                    ? BigDecimal.valueOf(avgSalaryNum).setScale(2, RoundingMode.HALF_UP)
                    : null;

            departmentStats.add(new DepartmentStatDto(deptId, deptName, deptCode, count, activeCount, deptPercentage, avgSalary));
        }
        return departmentStats;
    }
}
