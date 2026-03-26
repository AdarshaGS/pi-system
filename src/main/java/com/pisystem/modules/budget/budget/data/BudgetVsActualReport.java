package com.budget.data;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing complete budget vs actual report for a month
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetVsActualReport {
    
    private YearMonth month;
    private BigDecimal totalBudget;
    private BigDecimal totalSpent;
    private BigDecimal totalVariance;
    private BigDecimal variancePercentage;
    private String overallStatus;
    private List<BudgetVarianceAnalysis> categoryBreakdown;
    private BudgetPerformanceMetrics metrics;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BudgetPerformanceMetrics {
        private Integer categoriesOverBudget;
        private Integer categoriesUnderBudget;
        private Integer categoriesOnTrack;
        private Integer categoriesWithNoBudget;
        private BigDecimal averageVariancePercentage;
        private String worstCategory;
        private BigDecimal worstCategoryVariance;
        private String bestCategory;
        private BigDecimal bestCategoryVariance;
    }
}
