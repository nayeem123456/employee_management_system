package com.example.employeemanagement.dto;

import java.util.Set;

public class AuthResponseDto {

    private String username;
    private String fullName;
    private Set<String> roles;
    private boolean isAdmin;
    private String message;

    public AuthResponseDto() {
    }

    public AuthResponseDto(String username, String fullName, Set<String> roles, boolean isAdmin, String message) {
        this.username = username;
        this.fullName = fullName;
        this.roles = roles;
        this.isAdmin = isAdmin;
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
