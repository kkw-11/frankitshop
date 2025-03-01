package com.shop.frankit.exception;

import com.shop.frankit.dto.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Custom exception handling
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<?>> handleAppException(AppException ex) {
        log.error("Application exception occurred: code={}, message={}", ex.getErrorCode(), ex.getMessage());

        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
            ex.getErrorCode(), ex.getMessage());

        return ResponseEntity.status(determineStatus(ex))
            .body(ApiResponse.error(ex.getMessage(), errorDetails));
    }

    // Authentication exception handling
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse<?>> handleAuthenticationException(Exception ex) {
        log.warn("Authentication failed: {}", ex.getMessage());

        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
            "AUTH_001", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("Authentication failed", errorDetails));
    }

    // Access denied exception handling
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
            "AUTH_004", "Access denied");

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error("Access has been denied", errorDetails));
    }

    // Validation exception handling
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.debug("Validation failed: {}", errors);

        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
            "VALIDATION_001", errors.toString());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Validation failed", errorDetails));
    }

    // Global exception handling
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(Exception ex) {
        log.error("Unexpected error occurred", ex);

        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
            "SERVER_001", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Internal server error occurred", errorDetails));
    }

    private HttpStatus determineStatus(AppException ex) {
        if (ex instanceof AuthException) {
            return HttpStatus.UNAUTHORIZED;
        } else if (ex instanceof ResourceNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.BAD_REQUEST;
    }
}