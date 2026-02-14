package com.common.exception;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.common.exception.data.CriticalLog;
import com.common.exception.repo.CriticalLogRepository;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.access.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger CRITICAL_LOGGER = LoggerFactory.getLogger("CRITICAL_LOGGER");

    private final CriticalLogRepository criticalLogRepository;

    public GlobalExceptionHandler(CriticalLogRepository criticalLogRepository) {
        this.criticalLogRepository = criticalLogRepository;
    }

    /**
     * Safely extracts stack trace as string without using printStackTrace()
     */
    private String getStackTraceAsString(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getName());
        if (throwable.getMessage() != null) {
            sb.append(": ").append(throwable.getMessage());
        }
        sb.append("\n");
        
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        
        if (throwable.getCause() != null) {
            sb.append("Caused by: ").append(getStackTraceAsString(throwable.getCause()));
        }
        
        return sb.toString();
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(Exception ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .requestId(MDC.get("requestId"))
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message("Access denied: You do not have permission to access this resource.")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .requestId(MDC.get("requestId"))
                .status(ex.getStatus().value())
                .error(ex.getCode())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        // 1. Log to File (Reliable backup)
        CRITICAL_LOGGER.error("CRITICAL FAILURE | ID: {} | Method: {} | Path: {} | Exception: {} | Message: {}",
                MDC.get("requestId"),
                request.getMethod(),
                request.getRequestURI(),
                ex.getClass().getName(),
                ex.getMessage(),
                ex);

        // 2. Persist to Database (Searchable history)
        try {
            String stackTrace = getStackTraceAsString(ex);

            CriticalLog errorLog = CriticalLog.builder()
                    .timestamp(LocalDateTime.now())
                    .requestId(MDC.get("requestId"))
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .errorCode(ex.getClass().getSimpleName())
                    .message(ex.getMessage())
                    .path(request.getRequestURI())
                    .method(request.getMethod())
                    .stackTrace(stackTrace)
                    .build();

            criticalLogRepository.save(errorLog);
        } catch (Exception dbEx) {
            CRITICAL_LOGGER.error("FAILED TO PERSIST ERROR LOG TO DB: {}", dbEx.getMessage());
        }

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .requestId(MDC.get("requestId"))
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected system error occurred. Please contact support.") // Hide internal details
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}