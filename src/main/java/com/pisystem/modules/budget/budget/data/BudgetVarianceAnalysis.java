package com.budget.data;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing budget vs actual variance analysis for a specific category
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetVarianceAnalysis {
    
    private String category;
    private BigDecimal budgetAmount;
    private BigDecimal actualSpent;
    private BigDecimal variance;
    private BigDecimal variancePercentage;
    private VarianceStatus status;
    private BigDecimal remaining;
    private Integer transactionCount;
    
    public enum VarianceStatus {
        UNDER_BUDGET,    // Spending less than budget
        ON_TRACK,        // Within 90-100% of budget
        OVER_BUDGET,     // Exceeded budget
        NO_BUDGET        // No budget set for this category
    }
}
