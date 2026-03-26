package com.budget.data;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class BudgetReportDTO {
    private String monthYear;
    private BigDecimal totalBudget;
    private BigDecimal totalSpent;
    private BigDecimal totalExpenses;
    private BigDecimal totalIncome;
    private BigDecimal balance;
    private BigDecimal remainingBudget;
    private BigDecimal savings;
    private Double budgetUsagePercentage;
    private Map<ExpenseCategory, CategorySummary> categoryBreakdown;
    private Map<String, BigDecimal> categoryBudgets;
    private List<Expense> recentExpenses;
    private List<Income> recentIncomes;

    @Data
    @Builder
    public static class CategorySummary {
        private BigDecimal limit;
        private BigDecimal spent;
        private BigDecimal remaining;
        private double percentageUsed;
    }
}
