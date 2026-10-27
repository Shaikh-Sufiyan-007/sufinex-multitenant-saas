package com.example.multitenant.dto.response;

import java.time.Instant;
import java.util.Map;

public class ErrorResponse {

    private Instant timestamp;
    private int status;
    private String message;
    private String path;
    private Map<String, String> errors;

    public ErrorResponse() {
        this.timestamp = Instant.now();
    }

    public ErrorResponse(int status, String message, String path) {
        this.timestamp = Instant.now();
        this.status = status;
        this.message = message;
        this.path = path;
    }

    public ErrorResponse(int status, String message, String path, Map<String, String> errors) {
        this.timestamp = Instant.now();
        this.status = status;
        this.message = message;
        this.path = path;
        this.errors = errors;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}
