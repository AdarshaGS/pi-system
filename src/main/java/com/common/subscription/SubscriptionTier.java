package com.common.subscription;

/**
 * Enum representing user subscription tiers
 */
public enum SubscriptionTier {
    FREE("Free", "Basic features with limitations"),
    PREMIUM("Premium", "Full access to all features"),
    ENTERPRISE("Enterprise", "Custom features for business");

    private final String displayName;
    private final String description;

    SubscriptionTier(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
