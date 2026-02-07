package com.budget.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.common.exception.data.CriticalLog;
import com.common.exception.repo.CriticalLogRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for Budget module
 * Sprint 4 - Comprehensive Error Handling
 */
@RestControllerAdvice
public class BudgetExceptionHandler {

    private static final Logger CRITICAL_LOGGER = LoggerFactory.getLogger("CRITICAL_LOGGER");
    private final CriticalLogRepository criticalLogRepository;

    public BudgetExceptionHandler(CriticalLogRepository criticalLogRepository) {
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

    @ExceptionHandler(ExpenseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleExpenseNotFound(
            ExpenseNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(IncomeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleIncomeNotFound(
            IncomeNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BudgetNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBudgetNotFound(
            BudgetNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidBudgetException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBudget(
            InvalidBudgetException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Input validation failed. Please check the provided data.",
                request.getRequestURI(),
                validationErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        // 1. Log to File (Reliable backup)
        CRITICAL_LOGGER.error("BUDGET MODULE CRITICAL FAILURE | ID: {} | Method: {} | Path: {} | Exception: {} | Message: {}",
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
            CRITICAL_LOGGER.error("FAILED TO PERSIST BUDGET ERROR LOG TO DB: {}", dbEx.getMessage());
        }
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}