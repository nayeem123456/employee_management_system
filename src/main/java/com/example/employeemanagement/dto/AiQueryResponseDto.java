package com.example.employeemanagement.dto;

import java.time.Instant;
import java.util.Map;

public class AiQueryResponseDto {

    private String question;
    private String answer;
    private String source; // "AI_LLM" or "RULE_BASED"
    private Instant timestamp;
    private Map<String, Object> facts;

    public AiQueryResponseDto() {
        this.timestamp = Instant.now();
    }

    public AiQueryResponseDto(String question, String answer, String source, Map<String, Object> facts) {
        this.question = question;
        this.answer = answer;
        this.source = source;
        this.facts = facts;
        this.timestamp = Instant.now();
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getFacts() {
        return facts;
    }

    public void setFacts(Map<String, Object> facts) {
        this.facts = facts;
    }
}
