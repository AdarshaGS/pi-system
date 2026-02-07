package com.common.subscription;

import lombok.Data;

/**
 * DTO for tier limits information
 */
@Data
public class TierLimitsDTO {
    private SubscriptionTier tier;
    private int maxStocks;
    private int maxBudgetCategories;
    private int maxPolicies;
    private boolean upiAlwaysFree;
    private boolean loanCalculatorFree;
}
