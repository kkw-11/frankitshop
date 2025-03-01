package com.shop.frankit.exception;

import com.shop.frankit.dto.common.ApiResponse;
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

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 사용자 정의 예외 처리
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<?>> handleAppException(AppException ex) {
        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
            ex.getErrorCode(), ex.getMessage());

        return ResponseEntity.status(determineStatus(ex))
            .body(ApiResponse.error(ex.getMessage(), errorDetails));
    }

    // 인증 예외 처리
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse<?>> handleAuthenticationException(Exception ex) {
        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
            "AUTH_001", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("인증에 실패했습니다", errorDetails));
    }

    // 접근 권한 예외 처리
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException ex) {
        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
            "AUTH_004", "접근 권한이 없습니다");

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error("접근이 거부되었습니다", errorDetails));
    }

    // 유효성 검사 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
            "VALIDATION_001", errors.toString());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("유효성 검사에 실패했습니다", errorDetails));
    }

    // 기타 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(Exception ex) {
        ApiResponse.ErrorDetails errorDetails = new ApiResponse.ErrorDetails(
            "SERVER_001", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("서버 내부 오류가 발생했습니다", errorDetails));
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
