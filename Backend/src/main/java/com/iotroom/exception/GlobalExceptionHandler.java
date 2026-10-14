package com.iotroom.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.iotroom.dto.response.ApiResponse;
import com.iotroom.enums.ErrorCode;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(ApiResponse.error(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse(ErrorCode.INVALID_KEY.getMessage());

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.INVALID_KEY.getCode(), message));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException exception) {
        return ResponseEntity.status(ErrorCode.UNAUTHORIZED.getStatusCode())
                .body(ApiResponse.error(
                        ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage()
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(
            DataIntegrityViolationException exception
    ) {
        return ResponseEntity.status(ErrorCode.DATA_INTEGRITY_VIOLATION.getStatusCode())
                .body(ApiResponse.error(
                        ErrorCode.DATA_INTEGRITY_VIOLATION.getCode(),
                        ErrorCode.DATA_INTEGRITY_VIOLATION.getMessage()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        log.error("Unhandled exception occurred", exception);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error(
                        ErrorCode.UNCATEGORIZED_EXCEPTION.getCode(),
                        ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage()
                ));
    }
}

