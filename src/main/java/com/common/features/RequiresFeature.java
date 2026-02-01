package com.common.features;

import java.lang.annotation.*;

/**
 * Annotation to mark methods that require a specific feature to be enabled
 * Usage: @RequiresFeature(FeatureFlag.SUBSCRIPTIONS)
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresFeature {
    
    /**
     * The feature flag that must be enabled
     */
    FeatureFlag value();
    
    /**
     * Custom error message if feature is disabled
     */
    String message() default "";
}
