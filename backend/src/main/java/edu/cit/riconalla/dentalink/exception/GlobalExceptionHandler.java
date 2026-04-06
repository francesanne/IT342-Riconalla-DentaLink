package edu.cit.riconalla.dentalink.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String msg = ex.getMessage() != null ? ex.getMessage() : "An error occurred";

        if (msg.contains("already booked") || msg.contains("Conflict")) status = HttpStatus.CONFLICT;
        else if (msg.contains("not found") || msg.contains("Not Found")) status = HttpStatus.NOT_FOUND;
        else if (msg.contains("Forbidden") || msg.contains("Access denied")) status = HttpStatus.FORBIDDEN;

        return ResponseEntity.status(status).body(Map.of(
                "success", false,
                "error", Map.of("message", msg),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", Map.of("message", "Internal server error"),
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}