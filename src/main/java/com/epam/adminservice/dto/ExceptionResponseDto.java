package com.epam.adminservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for exception response.
 */
public class ExceptionResponseDto {

    private String message;
    @JsonInclude(Include.NON_EMPTY)
    private String description;
    @JsonInclude(Include.NON_EMPTY)
    private List<String> stackTrace;
    private LocalDateTime timestamp;

    public ExceptionResponseDto(String message, LocalDateTime timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public ExceptionResponseDto(String message, List<String> stackTrace, LocalDateTime timestamp) {
        this.message = message;
        this.stackTrace = stackTrace;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(List<String> stackTrace) {
        this.stackTrace = stackTrace;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
