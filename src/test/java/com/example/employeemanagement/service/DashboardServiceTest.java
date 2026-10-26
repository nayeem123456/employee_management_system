package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.DashboardSummaryDto;
import com.example.employeemanagement.entity.Department;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.entity.EmploymentStatus;
import com.example.employeemanagement.mapper.EmployeeMapper;
import com.example.employeemanagement.repository.DepartmentRepository;
import com.example.employeemanagement.repository.EmployeeRepository;
import com.example.employeemanagement.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    private final EmployeeMapper employeeMapper = new EmployeeMapper();

    private DashboardServiceImpl dashboardService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        dashboardService = new DashboardServiceImpl(employeeRepository, departmentRepository, employeeMapper);
    }

    @Test
    @DisplayName("Should aggregate dashboard summary counts and percentages accurately")
    void getDashboardSummary_Success() {
        Department dept = new Department("Engineering", "ENG", "Desc");
        dept.setId(1L);

        Employee emp = new Employee("Alice", "alice@company.com", dept, "Dev", "555-0101", LocalDate.now(), EmploymentStatus.ACTIVE, BigDecimal.valueOf(100000));
        emp.setId(1L);

        when(employeeRepository.count()).thenReturn(10L);
        when(employeeRepository.countByEmploymentStatus(EmploymentStatus.ACTIVE)).thenReturn(8L);
        when(employeeRepository.countByEmploymentStatus(EmploymentStatus.ON_LEAVE)).thenReturn(1L);
        when(employeeRepository.countByEmploymentStatus(EmploymentStatus.INACTIVE)).thenReturn(1L);

        // Object array matching getDepartmentAggregationStats: dept.id, dept.name, dept.code, count, activeCount, avgSalary
        Object[] deptRow = new Object[]{1L, "Engineering", "ENG", 10L, 8L, 95000.0};
        when(employeeRepository.getDepartmentAggregationStats()).thenReturn(java.util.Collections.singletonList(deptRow));
        when(employeeRepository.findTop5ByOrderByCreatedAtDesc()).thenReturn(List.of(emp));

        DashboardSummaryDto summary = dashboardService.getDashboardSummary();

        assertThat(summary).isNotNull();
        assertThat(summary.getTotalEmployees()).isEqualTo(10L);
        assertThat(summary.getActiveEmployees()).isEqualTo(8L);
        assertThat(summary.getOnLeaveEmployees()).isEqualTo(1L);
        assertThat(summary.getInactiveEmployees()).isEqualTo(1L);
        assertThat(summary.getActivePercentage()).isEqualTo(80.0);
        assertThat(summary.getDepartmentDistribution()).hasSize(1);
        assertThat(summary.getDepartmentDistribution().get(0).getDepartmentName()).isEqualTo("Engineering");
        assertThat(summary.getAiExecutiveSummary()).contains("Workforce Telemetry: Currently tracking 10 total personnel");
    }
}
