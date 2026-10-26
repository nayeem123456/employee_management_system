package com.example.employeemanagement.ai;

public interface AiClient {
    boolean isConfigured();
    String generateText(String prompt, String systemInstruction);
}
