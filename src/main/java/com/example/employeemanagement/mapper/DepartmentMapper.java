package com.example.employeemanagement.mapper;

import com.example.employeemanagement.dto.DepartmentDto;
import com.example.employeemanagement.entity.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public DepartmentDto toDto(Department department) {
        if (department == null) {
            return null;
        }

        DepartmentDto dto = new DepartmentDto();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setCode(department.getCode());
        dto.setDescription(department.getDescription());
        dto.setEmployeeCount(department.getEmployees() != null ? department.getEmployees().size() : 0);
        return dto;
    }
}
