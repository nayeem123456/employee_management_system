package com.example.employeemanagement.dto;

import java.util.List;

public class DashboardSummaryDto {

    private long totalEmployees;
    private long activeEmployees;
    private long onLeaveEmployees;
    private long inactiveEmployees;
    private double activePercentage;
    private List<DepartmentStatDto> departmentDistribution;
    private List<EmployeeResponseDto> recentEmployees;
    private String aiExecutiveSummary;

    public DashboardSummaryDto() {
    }

    public DashboardSummaryDto(long totalEmployees, long activeEmployees, long onLeaveEmployees,
                               long inactiveEmployees, double activePercentage,
                               List<DepartmentStatDto> departmentDistribution,
                               List<EmployeeResponseDto> recentEmployees,
                               String aiExecutiveSummary) {
        this.totalEmployees = totalEmployees;
        this.activeEmployees = activeEmployees;
        this.onLeaveEmployees = onLeaveEmployees;
        this.inactiveEmployees = inactiveEmployees;
        this.activePercentage = activePercentage;
        this.departmentDistribution = departmentDistribution;
        this.recentEmployees = recentEmployees;
        this.aiExecutiveSummary = aiExecutiveSummary;
    }

    public long getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(long totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public long getActiveEmployees() {
        return activeEmployees;
    }

    public void setActiveEmployees(long activeEmployees) {
        this.activeEmployees = activeEmployees;
    }

    public long getOnLeaveEmployees() {
        return onLeaveEmployees;
    }

    public void setOnLeaveEmployees(long onLeaveEmployees) {
        this.onLeaveEmployees = onLeaveEmployees;
    }

    public long getInactiveEmployees() {
        return inactiveEmployees;
    }

    public void setInactiveEmployees(long inactiveEmployees) {
        this.inactiveEmployees = inactiveEmployees;
    }

    public double getActivePercentage() {
        return activePercentage;
    }

    public void setActivePercentage(double activePercentage) {
        this.activePercentage = activePercentage;
    }

    public List<DepartmentStatDto> getDepartmentDistribution() {
        return departmentDistribution;
    }

    public void setDepartmentDistribution(List<DepartmentStatDto> departmentDistribution) {
        this.departmentDistribution = departmentDistribution;
    }

    public List<EmployeeResponseDto> getRecentEmployees() {
        return recentEmployees;
    }

    public void setRecentEmployees(List<EmployeeResponseDto> recentEmployees) {
        this.recentEmployees = recentEmployees;
    }

    public String getAiExecutiveSummary() {
        return aiExecutiveSummary;
    }

    public void setAiExecutiveSummary(String aiExecutiveSummary) {
        this.aiExecutiveSummary = aiExecutiveSummary;
    }
}
