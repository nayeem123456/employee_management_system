package com.example.employeemanagement.dto;

import java.time.LocalDate;

public class AiReportRequestDto {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String focusArea; // e.g. "ALL", "HEADCOUNT", "DEPARTMENTS", "TURNOVER"

    public AiReportRequestDto() {
    }

    public AiReportRequestDto(LocalDate dateFrom, LocalDate dateTo, String focusArea) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.focusArea = focusArea;
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

    public String getFocusArea() {
        return focusArea;
    }

    public void setFocusArea(String focusArea) {
        this.focusArea = focusArea;
    }
}
