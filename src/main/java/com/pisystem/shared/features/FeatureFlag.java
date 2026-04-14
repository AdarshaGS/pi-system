package com.pisystem.shared.features;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enum representing all feature flags in the system.
 *
 * <p>Two-level hierarchy:
 * <ul>
 *   <li><b>Module flags</b> – top-level gates (parentFlag == null).
 *       Disabling a module flag automatically disables all its sub-features.</li>
 *   <li><b>Sub-feature flags</b> – point to a parent module via parentFlag.
 *       They can be toggled individually, but only take effect when the parent is enabled.</li>
 * </ul>
 */
public enum FeatureFlag {

    // ── Module-level flags (parents) ─────────────────────────────────────────
    BUDGET_MODULE("Budget Module", "Budget and expense management with alerts, subscriptions, and recurring transactions", "budget", null),
    TAX_MODULE("Tax Module", "Tax planning, regime comparison, capital gains, and ITR management", "tax", null),
    INVESTMENTS_MODULE("Investments Module", "Portfolio tracking for stocks, bonds, gold, and real estate", "investments", null),
    BANKING_MODULE("Banking Module", "Manage bank accounts, credit cards, loans, FDs, and RDs", "banking", null),
    INSURANCE_MODULE("Insurance Module", "Track life, health, and other insurance policies", "insurance", null),
    NET_WORTH_MODULE("Net Worth Module", "Calculate net worth and track asset allocation", "networth", null),
    ADMIN_MODULE("Admin Module", "Administrative features including user management and audit logs", "admin", null),

    // ── Budget sub-features ───────────────────────────────────────────────────
    BUDGET_EXPENSES("Expenses", "Track and categorise spending transactions", "budget", BUDGET_MODULE),
    BUDGET_INCOME("Income", "Record and analyse income sources", "budget", BUDGET_MODULE),
    BUDGET_CATEGORIES("Categories", "Custom expense and income categories", "budget", BUDGET_MODULE),
    BUDGET_ALERTS("Budget Alerts", "Set spending threshold alerts", "budget", BUDGET_MODULE),
    BUDGET_SUBSCRIPTIONS("Subscriptions", "Track recurring subscription payments", "budget", BUDGET_MODULE),
    BUDGET_RECURRING_TRANSACTIONS("Recurring Transactions", "Automate recurring income/expense templates", "budget", BUDGET_MODULE),

    // ── Tax sub-features ──────────────────────────────────────────────────────
    TAX_CAPITAL_GAINS("Capital Gains", "Calculate short-term and long-term capital gains", "tax", TAX_MODULE),
    TAX_REGIME_COMPARISON("Regime Comparison", "Compare Old vs New income tax regimes", "tax", TAX_MODULE),
    TAX_TDS_TRACKING("TDS Tracking", "Track and reconcile TDS deductions", "tax", TAX_MODULE),
    TAX_ITR_ASSISTANT("ITR Assistant", "Step-by-step ITR filing guidance", "tax", TAX_MODULE),

    // ── Investments sub-features ──────────────────────────────────────────────
    INVESTMENTS_STOCKS("Stocks", "Track equity holdings and P&L", "investments", INVESTMENTS_MODULE),
    INVESTMENTS_MUTUAL_FUNDS("Mutual Funds", "Track mutual fund folios and NAV", "investments", INVESTMENTS_MODULE),
    INVESTMENTS_ETF("ETF", "Track ETF holdings", "investments", INVESTMENTS_MODULE),
    INVESTMENTS_BONDS("Bonds & Gold", "Track bonds, sovereign gold bonds, and gold", "investments", INVESTMENTS_MODULE),

    // ── Banking sub-features ──────────────────────────────────────────────────
    BANKING_ACCOUNTS("Bank Accounts", "Manage savings and current accounts", "banking", BANKING_MODULE),
    BANKING_LOANS("Loans", "EMI tracking and amortisation", "banking", BANKING_MODULE),
    BANKING_FIXED_DEPOSITS("Fixed Deposits", "Track FDs and RDs with maturity alerts", "banking", BANKING_MODULE),

    // ── Insurance sub-features ────────────────────────────────────────────────
    INSURANCE_LIFE("Life Insurance", "Track life cover policies and premiums", "insurance", INSURANCE_MODULE),
    INSURANCE_HEALTH("Health Insurance", "Track health policies and claim history", "insurance", INSURANCE_MODULE);

    // ─────────────────────────────────────────────────────────────────────────

    private final String displayName;
    private final String description;
    private final String category;
    private final FeatureFlag parentFlag;

    FeatureFlag(String displayName, String description, String category, FeatureFlag parentFlag) {
        this.displayName = displayName;
        this.description = description;
        this.category = category;
        this.parentFlag = parentFlag;
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

    /** Returns the parent module flag, or {@code null} if this is a module-level flag. */
    public FeatureFlag getParentFlag() {
        return parentFlag;
    }

    /** Returns {@code true} if this flag is a top-level module gate (has no parent). */
    public boolean isModuleFlag() {
        return parentFlag == null;
    }

    /** Returns all direct sub-features that belong to this module flag. */
    public List<FeatureFlag> getSubFeatures() {
        return Arrays.stream(values())
                .filter(f -> this.equals(f.parentFlag))
                .collect(Collectors.toList());
    }

    public String getKey() {
        return "features." + name().toLowerCase().replace("_", "-");
    }
}
