package edu.cit.riconalla.dentalink.dto;

import java.time.LocalDateTime;

public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ErrorDetail error;
    private String timestamp;

    private ApiResponse() {
        this.timestamp = LocalDateTime.now().toString();
    }

    // --- Factory methods ---

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.data = data;
        r.error = null;
        return r;
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.data = null;
        r.error = new ErrorDetail(code, message, null);
        return r;
    }

    // --- Getters ---

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public ErrorDetail getError() { return error; }
    public String getTimestamp() { return timestamp; }

    // --- Nested error structure per SDD §5.1 ---

    public static class ErrorDetail {
        private final String code;
        private final String message;
        private final Object details;

        public ErrorDetail(String code, String message, Object details) {
            this.code = code;
            this.message = message;
            this.details = details;
        }

        public String getCode() { return code; }
        public String getMessage() { return message; }
        public Object getDetails() { return details; }
    }
}