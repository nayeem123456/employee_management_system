package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.ApiResponseDto;
import com.example.employeemanagement.dto.AiQueryRequestDto;
import com.example.employeemanagement.dto.AiQueryResponseDto;
import com.example.employeemanagement.dto.AiReportRequestDto;
import com.example.employeemanagement.dto.AiReportResponseDto;
import com.example.employeemanagement.entity.AiReportHistory;
import com.example.employeemanagement.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Workforce Intelligence", description = "Endpoints for natural-language analytics, insights, and report generation")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/query")
    @Operation(summary = "Ask natural-language workforce analytics questions",
               description = "Answers questions like 'Which departments have the largest headcount?' using verified aggregate metrics.")
    public ResponseEntity<ApiResponseDto<AiQueryResponseDto>> queryAnalytics(@Valid @RequestBody AiQueryRequestDto requestDto) {
        AiQueryResponseDto response = aiService.queryAnalytics(requestDto.getQuestion());
        return ResponseEntity.ok(ApiResponseDto.ok(response));
    }

    @GetMapping("/insights")
    @Operation(summary = "Get automated workforce insights and trends",
               description = "Generates data-grounded observations and actionable recommendations.")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getWorkforceInsights() {
        Map<String, Object> insights = aiService.getWorkforceInsights();
        return ResponseEntity.ok(ApiResponseDto.ok(insights));
    }

    @PostMapping("/report")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate formal AI executive workforce report",
               description = "Restricted to ADMIN. Generates a structured workforce report with metrics, trends, and recommendations.")
    public ResponseEntity<ApiResponseDto<AiReportResponseDto>> generateReport(@RequestBody(required = false) AiReportRequestDto requestDto) {
        if (requestDto == null) {
            requestDto = new AiReportRequestDto();
        }
        AiReportResponseDto report = aiService.generateReport(requestDto);
        return ResponseEntity.ok(ApiResponseDto.ok("Report generated successfully", report));
    }

    @GetMapping("/reports/history")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get previous generated reports audit history",
               description = "Restricted to ADMIN. Returns the last 10 generated workforce reports.")
    public ResponseEntity<ApiResponseDto<List<AiReportHistory>>> getReportHistory() {
        List<AiReportHistory> history = aiService.getReportHistory();
        return ResponseEntity.ok(ApiResponseDto.ok(history));
    }
}
