package com.budget.data;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class CashFlowDTO {
    private String monthYear;
    
    // Income breakdown
    private BigDecimal totalIncome;
    private BigDecimal stableIncome;
    private BigDecimal variableIncome;
    private BigDecimal recurringIncome;
    private Map<String, BigDecimal> incomeBySource; // Salary, Dividend, Freelance, etc.
    
    // Expense breakdown
    private BigDecimal totalExpenses;
    private Map<ExpenseCategory, BigDecimal> expenseByCategory;
    
    // Cash flow metrics
    private BigDecimal netCashFlow; // Income - Expenses
    private BigDecimal savingsAmount;
    private Double savingsRate; // (Savings / Income) * 100
    private Double burnRate; // Monthly expense rate
    
    // Stability analysis
    private Double incomeStability; // % of stable income
    private Integer recurringIncomeCount;
    private Integer oneTimeIncomeCount;
    
    // Historical trends (if available)
    private List<MonthlyTrend> last6Months;
    
    @Data
    @Builder
    public static class MonthlyTrend {
        private String monthYear;
        private BigDecimal income;
        private BigDecimal expenses;
        private BigDecimal savings;
        private Double savingsRate;
    }
    
    // Insights
    private String cashFlowStatus; // POSITIVE, NEGATIVE, BREAK_EVEN
    private List<String> recommendations;
}
