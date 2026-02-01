package com.common.features;

/**
 * Enum representing all feature flags in the system
 * Each feature can be toggled on/off via configuration
 */
public enum FeatureFlag {
    
    // Budget Module Features
    BUDGET_MODULE("Budget Module", "Complete budget management with expense tracking", "budget"),
    EXPENSES("Expense Tracking", "Track and categorize expenses", "budget"),
    INCOME("Income Tracking", "Track income sources", "budget"),
    ALERTS("Budget Alerts", "Budget limit alerts and notifications", "budget"),
    RECURRING_TRANSACTIONS("Recurring Transactions", "Automated recurring transactions", "budget"),
    CUSTOM_CATEGORIES("Custom Categories", "User-defined expense categories", "budget"),
    CASH_FLOW_ANALYSIS("Cash Flow Analysis", "Cash flow analysis and reporting", "budget"),
    SUBSCRIPTIONS("Subscription Management", "Track recurring subscriptions", "budget"),
    
    // Tax Module Features
    TAX_MODULE("Tax Module", "Complete tax management and planning", "tax"),
    TAX_REGIME_COMPARISON("Tax Regime Comparison", "Compare Old vs New tax regime", "tax"),
    CAPITAL_GAINS("Capital Gains", "Track and calculate capital gains", "tax"),
    TAX_SAVING_RECOMMENDATIONS("Tax Saving Recommendations", "AI-powered tax saving suggestions", "tax"),
    TDS_TRACKING("TDS Tracking", "Track TDS entries and reconciliation", "tax"),
    TAX_PROJECTIONS("Tax Projections", "Project tax liability for current FY", "tax"),
    ITR_EXPORT("ITR Export", "Export ITR pre-fill data", "tax"),
    
    // Investment Module Features
    PORTFOLIO("Portfolio Management", "Track investment portfolio", "investments"),
    STOCKS("Stock Tracking", "Track stock investments", "investments"),
    MUTUAL_FUNDS("Mutual Funds", "Track mutual fund investments", "investments"),
    BONDS("Bonds", "Track bond investments", "investments"),
    GOLD("Gold", "Track gold investments", "investments"),
    ETF("ETF", "Track ETF investments", "investments"),
    REAL_ESTATE("Real Estate", "Track real estate investments", "investments"),
    
    // Banking Features
    BANK_ACCOUNTS("Bank Accounts", "Manage bank accounts", "banking"),
    CREDIT_CARDS("Credit Cards", "Track credit cards", "banking"),
    LOANS("Loans", "Track loans and EMIs", "banking"),
    FIXED_DEPOSITS("Fixed Deposits", "Track FD investments", "banking"),
    RECURRING_DEPOSITS("Recurring Deposits", "Track RD investments", "banking"),
    
    // Insurance Features
    INSURANCE("Insurance", "Track insurance policies", "insurance"),
    LIFE_INSURANCE("Life Insurance", "Track life insurance policies", "insurance"),
    HEALTH_INSURANCE("Health Insurance", "Track health insurance policies", "insurance"),
    
    // Net Worth Features
    NET_WORTH("Net Worth", "Calculate and track net worth", "networth"),
    ASSET_ALLOCATION("Asset Allocation", "Track asset allocation", "networth"),
    
    // Admin Features
    ADMIN_PORTAL("Admin Portal", "Administrative features", "admin"),
    USER_MANAGEMENT("User Management", "Manage users", "admin"),
    AUDIT_LOGS("Audit Logs", "View audit logs", "admin"),
    REPORTS("Reports", "Generate reports", "admin"),
    
    // Future Features (Placeholder)
    RECEIPT_MANAGEMENT("Receipt Management", "Upload and manage receipts", "budget"),
    SPLIT_EXPENSES("Split Expenses", "Split expenses with others", "budget"),
    BUDGET_FORECASTING("Budget Forecasting", "AI-powered budget predictions", "budget"),
    FINANCIAL_GOALS("Financial Goals", "Track financial goals", "planning"),
    MULTI_CURRENCY("Multi-Currency", "Support multiple currencies", "core");
    
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
