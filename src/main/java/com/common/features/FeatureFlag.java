package com.common.features;

/**
 * Enum representing all feature flags in the system
 * Simplified to one feature flag per module for easier management
 */
public enum FeatureFlag {
    
    // Module-level feature flags
    BUDGET_MODULE("Budget Module", "Budget and expense management with alerts, subscriptions, and recurring transactions", "budget"),
    TAX_MODULE("Tax Module", "Tax planning, regime comparison, capital gains, and ITR management", "tax"),
    INVESTMENTS_MODULE("Investments Module", "Portfolio tracking for stocks, mutual funds, bonds, gold, ETF, and real estate", "investments"),
    BANKING_MODULE("Banking Module", "Manage bank accounts, credit cards, loans, FDs, and RDs", "banking"),
    INSURANCE_MODULE("Insurance Module", "Track life, health, and other insurance policies", "insurance"),
    NET_WORTH_MODULE("Net Worth Module", "Calculate net worth and track asset allocation", "networth"),
    ADMIN_MODULE("Admin Module", "Administrative features including user management and audit logs", "admin");
    
    private final String displayName;
    private final String description;
    private final String category;
    
    FeatureFlag(String displayName, String description, String category) {
        this.displayName = displayName;
        this.description = description;
        this.category = category;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public String getKey() {
        return "features." + name().toLowerCase().replace("_", "-");
    }
}
