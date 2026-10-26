package com.example.employeemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AiQueryRequestDto {

    @NotBlank(message = "Question or analytics prompt cannot be blank")
    @Size(max = 500, message = "Question cannot exceed 500 characters")
    private String question;

    public AiQueryRequestDto() {
    }

    public AiQueryRequestDto(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
