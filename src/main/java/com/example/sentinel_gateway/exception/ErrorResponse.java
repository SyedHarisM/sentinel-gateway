package com.example.sentinel_gateway.exception;

import java.time.Instant;

public class ErrorResponse {

    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String method;
    private String path;
    private String correlationId;

    public ErrorResponse(
        Instant timestamp,
         int status,
          String error,
           String message,
            String method,
             String path,
              String correlationId
        ) {

        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.method = method;
        this.path = path;
        this.correlationId = correlationId;
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
    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getMethod() {
        return method;
    }
    public void setMethod(String method) {
        this.method = method;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getCorrelationId() {
        return correlationId;
    }
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
    
    
}
