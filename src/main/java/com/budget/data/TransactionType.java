package com.budget.data;

/**
 * Enum for transaction type (Expense, Income, Investments, Needs)
 */
public enum TransactionType {
    EXPENSE("Expense"),
    INCOME("Income"),
    INVESTMENTS("Investments"),
    NEEDS("Needs");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
