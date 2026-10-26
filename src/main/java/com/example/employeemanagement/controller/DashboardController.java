package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.ApiResponseDto;
import com.example.employeemanagement.dto.DashboardSummaryDto;
import com.example.employeemanagement.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Operational summaries and aggregated workforce telemetry")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get complete dashboard summary metrics", description = "Aggregates total, active, leave, inactive counts, department breakdown, recent hires, and AI summary.")
    public ResponseEntity<ApiResponseDto<DashboardSummaryDto>> getDashboardSummary() {
        DashboardSummaryDto summary = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponseDto.ok(summary));
    }
}
