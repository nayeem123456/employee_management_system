package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.EmployeeRequestDto;
import com.example.employeemanagement.dto.EmployeeResponseDto;
import com.example.employeemanagement.entity.EmploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {

    EmployeeResponseDto createEmployee(EmployeeRequestDto requestDto);

    EmployeeResponseDto getEmployeeById(Long id);

    Page<EmployeeResponseDto> getAllEmployees(Long departmentId, EmploymentStatus status, String query, Pageable pageable);

    EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto requestDto);

    EmployeeResponseDto updateEmployeeStatus(Long id, EmploymentStatus status);

    void deleteEmployee(Long id);

    Page<EmployeeResponseDto> searchEmployees(String query, Pageable pageable);
}
