package com.example.employeemanagement.mapper;

import com.example.employeemanagement.dto.EmployeeRequestDto;
import com.example.employeemanagement.dto.EmployeeResponseDto;
import com.example.employeemanagement.entity.Department;
import com.example.employeemanagement.entity.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public EmployeeResponseDto toDto(Employee employee, boolean includeSalary) {
        if (employee == null) {
            return null;
        }

        EmployeeResponseDto dto = new EmployeeResponseDto();
        dto.setId(employee.getId());
        dto.setFullName(employee.getFullName());
        dto.setEmail(employee.getEmail());
        if (employee.getDepartment() != null) {
            dto.setDepartmentId(employee.getDepartment().getId());
            dto.setDepartmentName(employee.getDepartment().getName());
            dto.setDepartmentCode(employee.getDepartment().getCode());
        }
        dto.setJobTitle(employee.getJobTitle());
        dto.setPhoneNumber(employee.getPhoneNumber());
        dto.setJoiningDate(employee.getJoiningDate());
        dto.setEmploymentStatus(employee.getEmploymentStatus());

        // Role-based salary protection: only authorized roles (ADMIN) can view salary
        if (includeSalary) {
            dto.setSalary(employee.getSalary());
        } else {
            dto.setSalary(null);
        }

        dto.setCreatedAt(employee.getCreatedAt());
        dto.setUpdatedAt(employee.getUpdatedAt());
        return dto;
    }

    public Employee toEntity(EmployeeRequestDto dto, Department department) {
        if (dto == null) {
            return null;
        }

        Employee employee = new Employee();
        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());
        employee.setDepartment(department);
        employee.setJobTitle(dto.getJobTitle());
        employee.setPhoneNumber(dto.getPhoneNumber());
        employee.setJoiningDate(dto.getJoiningDate());
        employee.setEmploymentStatus(dto.getEmploymentStatus());
        employee.setSalary(dto.getSalary());
        return employee;
    }

    public void updateEntityFromDto(Employee employee, EmployeeRequestDto dto, Department department) {
        if (employee == null || dto == null) {
            return;
        }

        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());
        employee.setDepartment(department);
        employee.setJobTitle(dto.getJobTitle());
        employee.setPhoneNumber(dto.getPhoneNumber());
        employee.setJoiningDate(dto.getJoiningDate());
        employee.setEmploymentStatus(dto.getEmploymentStatus());
        employee.setSalary(dto.getSalary());
    }
}
