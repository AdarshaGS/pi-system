package com.budget;

/**
 * Enum representing billing cycles for subscriptions
 */
public enum BillingCycle {
    MONTHLY("Monthly"),
    QUARTERLY("Quarterly"),
    HALF_YEARLY("Half-Yearly"),
    YEARLY("Yearly"),
    WEEKLY("Weekly");

    private final String displayName;

    BillingCycle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
