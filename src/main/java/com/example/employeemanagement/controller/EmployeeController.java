package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.ApiResponseDto;
import com.example.employeemanagement.dto.EmployeeRequestDto;
import com.example.employeemanagement.dto.EmployeeResponseDto;
import com.example.employeemanagement.dto.EmployeeStatusUpdateDto;
import com.example.employeemanagement.entity.EmploymentStatus;
import com.example.employeemanagement.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employee Management", description = "Endpoints for employee CRUD operations, filtering, and status updates")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new employee", description = "Restricted to ADMIN role. Creates a new employee with full details.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Employee created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error in input"),
            @ApiResponse(responseCode = "409", description = "Employee with this email already exists"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient privileges")
    })
    public ResponseEntity<ApiResponseDto<EmployeeResponseDto>> createEmployee(@Valid @RequestBody EmployeeRequestDto requestDto) {
        EmployeeResponseDto created = employeeService.createEmployee(requestDto);
        return new ResponseEntity<>(ApiResponseDto.ok("Employee created successfully", created), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all employees with pagination and filters", description = "Accessible by ADMIN and USER. Filter by department, status, or search query.")
    public ResponseEntity<ApiResponseDto<Page<EmployeeResponseDto>>> getAllEmployees(
            @Parameter(description = "Filter by department ID") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "Filter by employment status") @RequestParam(required = false) EmploymentStatus status,
            @Parameter(description = "Free-text search query across name, job title, email, department") @RequestParam(required = false) String query,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)") @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<EmployeeResponseDto> employees = employeeService.getAllEmployees(departmentId, status, query, pageable);
        return ResponseEntity.ok(ApiResponseDto.ok(employees));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID", description = "Accessible by ADMIN and USER. Salary is only revealed to ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee found"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "401", description = "Unauthenticated")
    })
    public ResponseEntity<ApiResponseDto<EmployeeResponseDto>> getEmployeeById(@PathVariable Long id) {
        EmployeeResponseDto employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponseDto.ok(employee));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update employee details", description = "Restricted to ADMIN role. Updates full details of an existing employee.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee updated successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "409", description = "Email conflict with another employee"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient privileges")
    })
    public ResponseEntity<ApiResponseDto<EmployeeResponseDto>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDto requestDto) {
        EmployeeResponseDto updated = employeeService.updateEmployee(id, requestDto);
        return ResponseEntity.ok(ApiResponseDto.ok("Employee updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update employee employment status", description = "Restricted to ADMIN role. Changes status to ACTIVE, ON_LEAVE, or INACTIVE.")
    public ResponseEntity<ApiResponseDto<EmployeeResponseDto>> updateEmployeeStatus(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeStatusUpdateDto statusDto) {
        EmployeeResponseDto updated = employeeService.updateEmployeeStatus(id, statusDto.getStatus());
        return ResponseEntity.ok(ApiResponseDto.ok("Employee status updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete employee", description = "Restricted to ADMIN role. Permanently deletes an employee record.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient privileges")
    })
    public ResponseEntity<ApiResponseDto<String>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponseDto.ok("Employee deleted successfully", "Record with ID " + id + " was deleted"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search employees", description = "Search employees across full name, job title, department name, or email.")
    public ResponseEntity<ApiResponseDto<Page<EmployeeResponseDto>>> searchEmployees(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<EmployeeResponseDto> employees = employeeService.searchEmployees(query, pageable);
        return ResponseEntity.ok(ApiResponseDto.ok(employees));
    }
}
