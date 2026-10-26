package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.DepartmentDto;
import com.example.employeemanagement.entity.Department;

import java.util.List;

public interface DepartmentService {
    List<DepartmentDto> getAllDepartments();
    Department getDepartmentEntityById(Long id);
    DepartmentDto getDepartmentById(Long id);
}
