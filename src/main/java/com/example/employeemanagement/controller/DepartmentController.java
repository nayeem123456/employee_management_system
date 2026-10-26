package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.ApiResponseDto;
import com.example.employeemanagement.dto.DepartmentDto;
import com.example.employeemanagement.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@Tag(name = "Departments", description = "Endpoints for accessing organizational departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    @Operation(summary = "Get all departments", description = "Returns all departments with current headcount numbers")
    public ResponseEntity<ApiResponseDto<List<DepartmentDto>>> getAllDepartments() {
        List<DepartmentDto> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(ApiResponseDto.ok(departments));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get department by ID", description = "Returns a single department by its ID")
    public ResponseEntity<ApiResponseDto<DepartmentDto>> getDepartmentById(@PathVariable Long id) {
        DepartmentDto department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponseDto.ok(department));
    }
}
