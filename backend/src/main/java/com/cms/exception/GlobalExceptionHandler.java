package com.cms.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Global exception handler for the entire application.
 * Intercepts exceptions thrown by controllers and services,
 * and returns standardized JSON error responses with appropriate HTTP status codes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles ResourceNotFoundException and returns 404 Not Found.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exception, WebRequest request) {
        // Task note: keeping this log at WARN helps during support without polluting error logs.
        log.warn("Resource not found: {}", exception.getMessage());
        return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), extractPath(request));
    }

    /**
     * Handles validation errors when request body validation fails.
     * Collects all field errors and returns them in a single error message.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception, WebRequest request) {
        List<String> details = exception.getBindingResult().getFieldErrors().stream()
            .map(this::formatFieldError)
            .collect(Collectors.toList());

        // Yesterday's validation task follow-up: include all field details in one clean response.
        String validationMessage = String.join(", ", details);
        log.debug("Validation failed: {}", validationMessage);
        return buildError(HttpStatus.BAD_REQUEST, validationMessage, extractPath(request));
    }

    /**
     * Handles invalid arguments from service/business rules as 400 responses.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException exception, WebRequest request) {
        log.warn("Business rule violation: {}", exception.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, exception.getMessage(), extractPath(request));
    }

    /**
     * Generic exception handler for any unhandled exceptions.
     * Logs the error and returns 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception exception, WebRequest request) {
        log.error("Unhandled application error", exception);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", extractPath(request));
    }

    /**
     * Helper method to construct an ApiError response with timestamp, status, error name, and message.
     */
    private ResponseEntity<ApiError> buildError(HttpStatus status, String message, String path) {
        ApiError error = new ApiError(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, path);
        return ResponseEntity.status(status).body(error);
    }

    /**
     * Formats field error messages from validation failures.
     */
    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    /**
     * Converts Spring's "uri=/path" request description into a cleaner path value.
     */
    private String extractPath(WebRequest request) {
        String description = request.getDescription(false);
        if (description != null && description.startsWith("uri=")) {
            return description.substring(4);
        }
        return description;
    }
}