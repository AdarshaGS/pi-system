package com.common.features;

/**
 * Exception thrown when a disabled feature is accessed
 */
public class FeatureNotEnabledException extends RuntimeException {
    
    private final String featureName;
    
    public FeatureNotEnabledException(String message) {
        super(message);
        this.featureName = null;
    }
    
    public FeatureNotEnabledException(String message, String featureName) {
        super(message);
        this.featureName = featureName;
    }
    
    public String getFeatureName() {
        return featureName;
    }
}
