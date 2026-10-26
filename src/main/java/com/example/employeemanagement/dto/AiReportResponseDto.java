package com.example.employeemanagement.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AiReportResponseDto {

    private Long reportId;
    private String title;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String executiveSummary;
    private Map<String, Object> keyMetrics;
    private List<DepartmentStatDto> departmentBreakdown;
    private List<String> observedTrends;
    private List<String> actionableRecommendations;
    private Instant generatedAt;
    private String source; // "AI_LLM" or "RULE_BASED"

    public AiReportResponseDto() {
        this.generatedAt = Instant.now();
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    public String getExecutiveSummary() {
        return executiveSummary;
    }

    public void setExecutiveSummary(String executiveSummary) {
        this.executiveSummary = executiveSummary;
    }

    public Map<String, Object> getKeyMetrics() {
        return keyMetrics;
    }

    public void setKeyMetrics(Map<String, Object> keyMetrics) {
        this.keyMetrics = keyMetrics;
    }

    public List<DepartmentStatDto> getDepartmentBreakdown() {
        return departmentBreakdown;
    }

    public void setDepartmentBreakdown(List<DepartmentStatDto> departmentBreakdown) {
        this.departmentBreakdown = departmentBreakdown;
    }

    public List<String> getObservedTrends() {
        return observedTrends;
    }

    public void setObservedTrends(List<String> observedTrends) {
        this.observedTrends = observedTrends;
    }

    public List<String> getActionableRecommendations() {
        return actionableRecommendations;
    }

    public void setActionableRecommendations(List<String> actionableRecommendations) {
        this.actionableRecommendations = actionableRecommendations;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
