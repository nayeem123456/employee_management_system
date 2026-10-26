package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.EmployeeRequestDto;
import com.example.employeemanagement.dto.EmployeeResponseDto;
import com.example.employeemanagement.entity.Department;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.entity.EmploymentStatus;
import com.example.employeemanagement.exception.DuplicateEmailException;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.mapper.EmployeeMapper;
import com.example.employeemanagement.repository.EmployeeRepository;
import com.example.employeemanagement.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentService departmentService;

    private final EmployeeMapper employeeMapper = new EmployeeMapper();

    private EmployeeServiceImpl employeeService;

    private Department department;
    private Employee employee;
    private EmployeeRequestDto requestDto;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeServiceImpl(employeeRepository, departmentService, employeeMapper);
        department = new Department("Engineering", "ENG", "Software Dev");
        department.setId(1L);

        employee = new Employee(
                "Jane Doe",
                "jane.doe@company.com",
                department,
                "Senior Software Engineer",
                "+1-555-1234",
                LocalDate.of(2023, 1, 15),
                EmploymentStatus.ACTIVE,
                new BigDecimal("120000.00")
        );
        employee.setId(10L);

        requestDto = new EmployeeRequestDto(
                "Jane Doe",
                "jane.doe@company.com",
                1L,
                "Senior Software Engineer",
                "+1-555-1234",
                LocalDate.of(2023, 1, 15),
                EmploymentStatus.ACTIVE,
                new BigDecimal("120000.00")
        );
    }

    @Test
    @DisplayName("Should create employee successfully when email is unique")
    void createEmployee_Success() {
        when(employeeRepository.existsByEmailIgnoreCase(requestDto.getEmail())).thenReturn(false);
        when(departmentService.getDepartmentEntityById(1L)).thenReturn(department);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        EmployeeResponseDto result = employeeService.createEmployee(requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getFullName()).isEqualTo("Jane Doe");
        assertThat(result.getEmail()).isEqualTo("jane.doe@company.com");
        assertThat(result.getDepartmentName()).isEqualTo("Engineering");

        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw DuplicateEmailException when email already exists")
    void createEmployee_DuplicateEmail_ThrowsException() {
        when(employeeRepository.existsByEmailIgnoreCase(requestDto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> employeeService.createEmployee(requestDto))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("jane.doe@company.com");

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should retrieve employee by ID successfully")
    void getEmployeeById_Success() {
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));

        EmployeeResponseDto result = employeeService.getEmployeeById(10L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getEmail()).isEqualTo("jane.doe@company.com");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when employee ID not found")
    void getEmployeeById_NotFound_ThrowsException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should update employee details successfully")
    void updateEmployee_Success() {
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmailIgnoreCaseAndIdNot(requestDto.getEmail(), 10L)).thenReturn(false);
        when(departmentService.getDepartmentEntityById(1L)).thenReturn(department);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        requestDto.setJobTitle("Staff Software Architect");
        EmployeeResponseDto result = employeeService.updateEmployee(10L, requestDto);

        assertThat(result).isNotNull();
        verify(employeeRepository).save(employee);
    }

    @Test
    @DisplayName("Should update employee status successfully")
    void updateEmployeeStatus_Success() {
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        EmployeeResponseDto result = employeeService.updateEmployeeStatus(10L, EmploymentStatus.ON_LEAVE);

        assertThat(result).isNotNull();
        assertThat(employee.getEmploymentStatus()).isEqualTo(EmploymentStatus.ON_LEAVE);
        verify(employeeRepository).save(employee);
    }

    @Test
    @DisplayName("Should delete employee when ID exists")
    void deleteEmployee_Success() {
        when(employeeRepository.existsById(10L)).thenReturn(true);

        employeeService.deleteEmployee(10L);

        verify(employeeRepository).deleteById(10L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent ID")
    void deleteEmployee_NotFound_ThrowsException() {
        when(employeeRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> employeeService.deleteEmployee(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(employeeRepository, never()).deleteById(anyLong());
    }
}
