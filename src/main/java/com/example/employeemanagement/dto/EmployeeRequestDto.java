package com.example.employeemanagement.dto;

import com.example.employeemanagement.entity.EmploymentStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EmployeeRequestDto {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 150, message = "Full name must be between 2 and 150 characters")
    private String fullName;

    @NotBlank(message = "Email address is required")
    @Email(message = "Email address must be valid")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @NotNull(message = "Department ID is required")
    @Positive(message = "Department ID must be a positive integer")
    private Long departmentId;

    @NotBlank(message = "Job title is required")
    @Size(min = 2, max = 100, message = "Job title must be between 2 and 100 characters")
    private String jobTitle;

    @NotBlank(message = "Phone number is required")
    @Size(min = 7, max = 30, message = "Phone number must be between 7 and 30 characters")
    private String phoneNumber;

    @NotNull(message = "Joining date is required")
    @PastOrPresent(message = "Joining date cannot be in the future")
    private LocalDate joiningDate;

    @NotNull(message = "Employment status is required (ACTIVE, ON_LEAVE, or INACTIVE)")
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;

    @NotNull(message = "Salary is required")
    @DecimalMin(value = "0.01", message = "Salary must be greater than zero")
    private BigDecimal salary;

    public EmployeeRequestDto() {
    }

    public EmployeeRequestDto(String fullName, String email, Long departmentId, String jobTitle,
                              String phoneNumber, LocalDate joiningDate, EmploymentStatus employmentStatus, BigDecimal salary) {
        this.fullName = fullName;
        this.email = email;
        this.departmentId = departmentId;
        this.jobTitle = jobTitle;
        this.phoneNumber = phoneNumber;
        this.joiningDate = joiningDate;
        this.employmentStatus = employmentStatus;
        this.salary = salary;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }

    public EmploymentStatus getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(EmploymentStatus employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }
}
