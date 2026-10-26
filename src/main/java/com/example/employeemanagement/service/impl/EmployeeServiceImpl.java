package com.example.employeemanagement.service.impl;

import com.example.employeemanagement.dto.EmployeeRequestDto;
import com.example.employeemanagement.dto.EmployeeResponseDto;
import com.example.employeemanagement.entity.Department;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.entity.EmploymentStatus;
import com.example.employeemanagement.exception.DuplicateEmailException;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.mapper.EmployeeMapper;
import com.example.employeemanagement.repository.EmployeeRepository;
import com.example.employeemanagement.security.SecurityUtils;
import com.example.employeemanagement.service.DepartmentService;
import com.example.employeemanagement.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository employeeRepository;
    private final DepartmentService departmentService;
    private final EmployeeMapper employeeMapper;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               DepartmentService departmentService,
                               EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.departmentService = departmentService;
        this.employeeMapper = employeeMapper;
    }

    @Override
    @Transactional
    public EmployeeResponseDto createEmployee(EmployeeRequestDto requestDto) {
        log.info("Admin creating new employee with email: {}", requestDto.getEmail());

        if (employeeRepository.existsByEmailIgnoreCase(requestDto.getEmail())) {
            throw new DuplicateEmailException(requestDto.getEmail());
        }

        Department department = departmentService.getDepartmentEntityById(requestDto.getDepartmentId());
        Employee employee = employeeMapper.toEntity(requestDto, department);
        Employee saved = employeeRepository.save(employee);

        log.info("Successfully created employee with ID: {}", saved.getId());
        return employeeMapper.toDto(saved, SecurityUtils.isAdmin());
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
        return employeeMapper.toDto(employee, SecurityUtils.isAdmin());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponseDto> getAllEmployees(Long departmentId, EmploymentStatus status, String query, Pageable pageable) {
        boolean isAdmin = SecurityUtils.isAdmin();
        Page<Employee> page;

        if (departmentId != null || status != null || (query != null && !query.trim().isEmpty())) {
            page = employeeRepository.filterEmployees(departmentId, status, query != null ? query.trim() : null, pageable);
        } else {
            page = employeeRepository.findAll(pageable);
        }

        return page.map(emp -> employeeMapper.toDto(emp, isAdmin));
    }

    @Override
    @Transactional
    public EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto requestDto) {
        log.info("Updating employee with ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        if (employeeRepository.existsByEmailIgnoreCaseAndIdNot(requestDto.getEmail(), id)) {
            throw new DuplicateEmailException(requestDto.getEmail());
        }

        Department department = departmentService.getDepartmentEntityById(requestDto.getDepartmentId());
        employeeMapper.updateEntityFromDto(employee, requestDto, department);
        Employee updated = employeeRepository.save(employee);

        log.info("Successfully updated employee with ID: {}", updated.getId());
        return employeeMapper.toDto(updated, SecurityUtils.isAdmin());
    }

    @Override
    @Transactional
    public EmployeeResponseDto updateEmployeeStatus(Long id, EmploymentStatus status) {
        log.info("Updating employment status for employee ID: {} to {}", id, status);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));

        employee.setEmploymentStatus(status);
        Employee updated = employeeRepository.save(employee);

        return employeeMapper.toDto(updated, SecurityUtils.isAdmin());
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        log.info("Deleting employee with ID: {}", id);

        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employee", "id", id);
        }

        employeeRepository.deleteById(id);
        log.info("Successfully deleted employee with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponseDto> searchEmployees(String query, Pageable pageable) {
        boolean isAdmin = SecurityUtils.isAdmin();
        Page<Employee> page = employeeRepository.searchEmployees(query != null ? query.trim() : "", pageable);
        return page.map(emp -> employeeMapper.toDto(emp, isAdmin));
    }
}
