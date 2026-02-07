package com.common.subscription;

/**
 * Exception thrown when user exceeds their subscription tier limits
 */
public class TierLimitExceededException extends RuntimeException {
    
    private final SubscriptionTier currentTier;
    private final String feature;
    private final int limit;
    
    public TierLimitExceededException(SubscriptionTier currentTier, String feature, int limit) {
        super(String.format("Your %s plan allows only %d %s. Please upgrade to access more.", 
            currentTier.getDisplayName(), limit, feature));
        this.currentTier = currentTier;
        this.feature = feature;
        this.limit = limit;
    }
    
    public SubscriptionTier getCurrentTier() {
        return currentTier;
    }
    
    public String getFeature() {
        return feature;
    }
    
    public int getLimit() {
        return limit;
    }
}
