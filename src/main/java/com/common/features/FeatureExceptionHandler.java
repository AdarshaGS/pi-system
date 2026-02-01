package com.common.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for feature toggle exceptions
 * Order = Ordered.HIGHEST_PRECEDENCE to ensure it's checked before GlobalExceptionHandler
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FeatureExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(FeatureExceptionHandler.class);
    
    /**
     * Handle FeatureNotEnabledException
     * Return 403 Forbidden with feature information
     */
    @ExceptionHandler(FeatureNotEnabledException.class)
    public ResponseEntity<Map<String, Object>> handleFeatureNotEnabled(FeatureNotEnabledException ex) {
        logger.warn("Feature not enabled: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("error", "FEATURE_NOT_ENABLED");
        response.put("message", ex.getMessage());
        response.put("featureName", ex.getFeatureName());
        response.put("status", 403);
        
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
}
