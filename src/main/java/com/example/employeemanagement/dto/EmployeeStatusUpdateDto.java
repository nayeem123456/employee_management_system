package com.example.employeemanagement.dto;

import com.example.employeemanagement.entity.EmploymentStatus;
import jakarta.validation.constraints.NotNull;

public class EmployeeStatusUpdateDto {

    @NotNull(message = "Employment status is required (ACTIVE, ON_LEAVE, or INACTIVE)")
    private EmploymentStatus status;

    public EmployeeStatusUpdateDto() {
    }

    public EmployeeStatusUpdateDto(EmploymentStatus status) {
        this.status = status;
    }

    public EmploymentStatus getStatus() {
        return status;
    }

    public void setStatus(EmploymentStatus status) {
        this.status = status;
    }
}
