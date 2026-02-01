package com.budget;

/**
 * Enum representing subscription status
 */
public enum SubscriptionStatus {
    ACTIVE("Active"),
    CANCELLED("Cancelled"),
    EXPIRED("Expired"),
    PAUSED("Paused");

    private final String displayName;

    SubscriptionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
