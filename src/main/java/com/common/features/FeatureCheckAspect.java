package com.common.features;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Aspect to handle @RequiresFeature annotation
 * Checks if feature is enabled before allowing method execution
 */
@Aspect
@Component
public class FeatureCheckAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(FeatureCheckAspect.class);
    
    @Autowired
    private FeatureConfigService featureConfigService;
    
    @Around("@annotation(com.common.features.RequiresFeature) || @within(com.common.features.RequiresFeature)")
    public Object checkFeature(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // Check method-level annotation first
        RequiresFeature annotation = method.getAnnotation(RequiresFeature.class);
        
        // If not on method, check class-level
        if (annotation == null) {
            annotation = method.getDeclaringClass().getAnnotation(RequiresFeature.class);
        }
        
        if (annotation != null) {
            FeatureFlag requiredFeature = annotation.value();
            
            logger.debug("Checking feature flag: {} for method: {}", 
                    requiredFeature.name(), method.getName());
            
            if (!featureConfigService.isFeatureEnabled(requiredFeature)) {
                String message = annotation.message();
                if (message == null || message.isEmpty()) {
                    message = "Feature '" + requiredFeature.getDisplayName() + "' is not enabled";
                }
                
                logger.warn("Feature not enabled: {} for method: {}", 
                        requiredFeature.name(), method.getName());
                
                throw new FeatureNotEnabledException(message, requiredFeature.name());
            }
        }
        
        // Feature is enabled, proceed with method execution
        return joinPoint.proceed();
    }
}
