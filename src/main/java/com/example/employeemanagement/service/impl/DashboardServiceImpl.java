package com.example.employeemanagement.service.impl;

import com.example.employeemanagement.dto.DashboardSummaryDto;
import com.example.employeemanagement.dto.DepartmentStatDto;
import com.example.employeemanagement.dto.EmployeeResponseDto;
import com.example.employeemanagement.entity.EmploymentStatus;
import com.example.employeemanagement.mapper.EmployeeMapper;
import com.example.employeemanagement.repository.DepartmentRepository;
import com.example.employeemanagement.repository.EmployeeRepository;
import com.example.employeemanagement.security.SecurityUtils;
import com.example.employeemanagement.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeMapper employeeMapper;

    public DashboardServiceImpl(EmployeeRepository employeeRepository,
                                DepartmentRepository departmentRepository,
                                EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.employeeMapper = employeeMapper;
    }

    @Override
    public DashboardSummaryDto getDashboardSummary() {
        long totalEmployees = employeeRepository.count();
        long activeEmployees = employeeRepository.countByEmploymentStatus(EmploymentStatus.ACTIVE);
        long onLeaveEmployees = employeeRepository.countByEmploymentStatus(EmploymentStatus.ON_LEAVE);
        long inactiveEmployees = employeeRepository.countByEmploymentStatus(EmploymentStatus.INACTIVE);

        double activePercentage = totalEmployees > 0
                ? BigDecimal.valueOf((double) activeEmployees / totalEmployees * 100)
                    .setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        List<DepartmentStatDto> departmentDistribution = new ArrayList<>();
        List<Object[]> rawStats = employeeRepository.getDepartmentAggregationStats();
        boolean isAdmin = SecurityUtils.isAdmin();

        for (Object[] row : rawStats) {
            Long deptId = ((Number) row[0]).longValue();
            String deptName = (String) row[1];
            String deptCode = (String) row[2];
            long count = ((Number) row[3]).longValue();
            long activeCount = ((Number) row[4]).longValue();
            Double avgSalaryNum = row[5] != null ? ((Number) row[5]).doubleValue() : null;

            double deptPercentage = totalEmployees > 0
                    ? BigDecimal.valueOf((double) count / totalEmployees * 100)
                        .setScale(1, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;

            BigDecimal avgSalary = (isAdmin && avgSalaryNum != null)
                    ? BigDecimal.valueOf(avgSalaryNum).setScale(2, RoundingMode.HALF_UP)
                    : null;

            departmentDistribution.add(new DepartmentStatDto(
                    deptId, deptName, deptCode, count, activeCount, deptPercentage, avgSalary
            ));
        }

        List<EmployeeResponseDto> recentEmployees = employeeRepository.findTop5ByOrderByCreatedAtDesc().stream()
                .map(emp -> employeeMapper.toDto(emp, isAdmin))
                .collect(Collectors.toList());

        String aiExecutiveSummary = generateQuickSummary(totalEmployees, activeEmployees, onLeaveEmployees, inactiveEmployees, departmentDistribution);

        return new DashboardSummaryDto(
                totalEmployees,
                activeEmployees,
                onLeaveEmployees,
                inactiveEmployees,
                activePercentage,
                departmentDistribution,
                recentEmployees,
                aiExecutiveSummary
        );
    }

    private String generateQuickSummary(long total, long active, long onLeave, long inactive, List<DepartmentStatDto> depts) {
        if (total == 0) {
            return "No employee records are currently registered in the system. Add employees to generate workforce telemetry.";
        }

        String topDeptInfo = "";
        if (!depts.isEmpty()) {
            DepartmentStatDto top = depts.get(0);
            topDeptInfo = String.format(" The highest headcount resides in %s (%d team members, %.1f%% of workforce).",
                    top.getDepartmentName(), top.getTotalHeadcount(), top.getPercentage());
        }

        double activeRatio = (double) active / total * 100;
        String healthStatus = activeRatio >= 85.0 ? "healthy operational capacity" : "notable proportion of non-active personnel requiring review";

        return String.format(
                "Workforce Telemetry: Currently tracking %d total personnel across %d active departments. %d are active (%.1f%%), %d on temporary leave, and %d inactive, reflecting %s.%s",
                total, depts.size(), active, activeRatio, onLeave, inactive, healthStatus, topDeptInfo
        );
    }
}
