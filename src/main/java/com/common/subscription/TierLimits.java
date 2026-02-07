package com.common.subscription;

/**
 * Constants defining limits for each subscription tier
 */
public class TierLimits {
    
    // Portfolio limits
    public static final int FREE_MAX_STOCKS = 20;
    public static final int PREMIUM_MAX_STOCKS = Integer.MAX_VALUE; // Unlimited
    
    // Budget limits
    public static final int FREE_MAX_CATEGORIES = 5;
    public static final int PREMIUM_MAX_CATEGORIES = Integer.MAX_VALUE; // Unlimited
    
    // Insurance limits
    public static final int FREE_MAX_POLICIES = 2;
    public static final int PREMIUM_MAX_POLICIES = Integer.MAX_VALUE; // Unlimited
    
    // UPI is always free (no limits)
    public static final boolean UPI_ALWAYS_FREE = true;
    
    // Loan calculator is free for basic features
    public static final boolean LOAN_CALCULATOR_FREE = true;
    
    private TierLimits() {
        // Utility class
    }
    
    /**
     * Get maximum stocks allowed for a tier
     */
    public static int getMaxStocks(SubscriptionTier tier) {
        return switch (tier) {
            case FREE -> FREE_MAX_STOCKS;
            case PREMIUM, ENTERPRISE -> PREMIUM_MAX_STOCKS;
        };
    }
    
    /**
     * Get maximum budget categories allowed for a tier
     */
    public static int getMaxBudgetCategories(SubscriptionTier tier) {
        return switch (tier) {
            case FREE -> FREE_MAX_CATEGORIES;
            case PREMIUM, ENTERPRISE -> PREMIUM_MAX_CATEGORIES;
        };
    }
    
    /**
     * Get maximum insurance policies allowed for a tier
     */
    public static int getMaxPolicies(SubscriptionTier tier) {
        return switch (tier) {
            case FREE -> FREE_MAX_POLICIES;
            case PREMIUM, ENTERPRISE -> PREMIUM_MAX_POLICIES;
        };
    }
}
