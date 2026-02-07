package com.common.subscription;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler for subscription tier limit exceptions
 */
@ControllerAdvice
public class TierLimitExceptionHandler {
    
    @ExceptionHandler(TierLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleTierLimitExceeded(TierLimitExceededException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.PAYMENT_REQUIRED.value());
        body.put("error", "Subscription Limit Exceeded");
        body.put("message", ex.getMessage());
        body.put("currentTier", ex.getCurrentTier().getDisplayName());
        body.put("feature", ex.getFeature());
        body.put("limit", ex.getLimit());
        body.put("upgradeRequired", true);
        
        return new ResponseEntity<>(body, HttpStatus.PAYMENT_REQUIRED);
    }
}
