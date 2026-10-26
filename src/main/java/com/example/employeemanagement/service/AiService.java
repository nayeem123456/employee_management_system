package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.AiQueryResponseDto;
import com.example.employeemanagement.dto.AiReportRequestDto;
import com.example.employeemanagement.dto.AiReportResponseDto;
import com.example.employeemanagement.entity.AiReportHistory;

import java.util.List;
import java.util.Map;

public interface AiService {

    AiQueryResponseDto queryAnalytics(String query);

    Map<String, Object> getWorkforceInsights();

    AiReportResponseDto generateReport(AiReportRequestDto requestDto);

    List<AiReportHistory> getReportHistory();
}
