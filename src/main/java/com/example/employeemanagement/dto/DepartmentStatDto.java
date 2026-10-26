package com.example.employeemanagement.dto;

import java.math.BigDecimal;

public class DepartmentStatDto {

    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private long totalHeadcount;
    private long activeHeadcount;
    private double percentage;
    private BigDecimal averageSalary; // Included only for authorized views/analytics

    public DepartmentStatDto() {
    }

    public DepartmentStatDto(Long departmentId, String departmentName, String departmentCode,
                             long totalHeadcount, long activeHeadcount, double percentage, BigDecimal averageSalary) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.departmentCode = departmentCode;
        this.totalHeadcount = totalHeadcount;
        this.activeHeadcount = activeHeadcount;
        this.percentage = percentage;
        this.averageSalary = averageSalary;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public long getTotalHeadcount() {
        return totalHeadcount;
    }

    public void setTotalHeadcount(long totalHeadcount) {
        this.totalHeadcount = totalHeadcount;
    }

    public long getActiveHeadcount() {
        return activeHeadcount;
    }

    public void setActiveHeadcount(long activeHeadcount) {
        this.activeHeadcount = activeHeadcount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public BigDecimal getAverageSalary() {
        return averageSalary;
    }

    public void setAverageSalary(BigDecimal averageSalary) {
        this.averageSalary = averageSalary;
    }
}
