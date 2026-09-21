package io.virinchi.dhammanature.restapi;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Single place that turns exceptions thrown by the REST API into a consistent
 * {@code {status, message, timestamp}} JSON body, so controllers do not need
 * their own error() helpers or catch (Exception e) blocks.
 */
@RestControllerAdvice(basePackages = "io.virinchi.dhammanature.restapi")
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApi(ApiException ex) {
        return build(ex.getStatus().value(), ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex) {
        return build(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(IllegalStateException ex) {
        return build(HttpStatus.CONFLICT.value(), ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleIntegrity(DataIntegrityViolationException ex) {
        return build(HttpStatus.CONFLICT.value(), "Could not save - the data conflicts with an existing record.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST.value(),
                message.isBlank() ? "Validation failed." : message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Something went wrong. Please try again.");
    }

    private ResponseEntity<Map<String, Object>> build(int status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        body.put("message", message);
        body.put("timestamp", OffsetDateTime.now().toString());
        return ResponseEntity.status(status).body(body);
    }
}
