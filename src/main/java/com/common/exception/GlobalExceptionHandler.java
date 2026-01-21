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

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger CRITICAL_LOGGER = LoggerFactory.getLogger("CRITICAL_LOGGER");

    private final CriticalLogRepository criticalLogRepository;

    public GlobalExceptionHandler(CriticalLogRepository criticalLogRepository) {
        this.criticalLogRepository = criticalLogRepository;
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
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            ex.printStackTrace(pw);

            CriticalLog errorLog = CriticalLog.builder()
                    .timestamp(LocalDateTime.now())
                    .requestId(MDC.get("requestId"))
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .errorCode(ex.getClass().getSimpleName())
                    .message(ex.getMessage())
                    .path(request.getRequestURI())
                    .method(request.getMethod())
                    .stackTrace(sw.toString())
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