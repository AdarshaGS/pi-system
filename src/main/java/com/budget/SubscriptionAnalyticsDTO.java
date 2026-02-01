package com.budget;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for subscription analytics and cost analysis
 */
public class SubscriptionAnalyticsDTO {

    private Integer totalSubscriptions;
    private Integer activeSubscriptions;
    private Integer cancelledSubscriptions;
    private Integer unusedSubscriptions;
    private BigDecimal totalMonthlyCost;
    private BigDecimal totalAnnualCost;
    private BigDecimal potentialSavings; // From unused subscriptions
    private Map<SubscriptionCategory, CategorySpending> spendingByCategory;
    private List<SubscriptionDTO> upcomingRenewals; // Next 30 days
    private List<SubscriptionDTO> unusedSubscriptionsList;
    private Map<BillingCycle, Integer> subscriptionsByBillingCycle;
    private SubscriptionDTO mostExpensiveSubscription;
    private String topCategory; // Category with most spending

    // Inner class for category spending
    public static class CategorySpending {
        private SubscriptionCategory category;
        private Integer count;
        private BigDecimal monthlySpending;
        private BigDecimal annualSpending;
        private Double percentageOfTotal;

        public CategorySpending() {
        }

        public CategorySpending(SubscriptionCategory category, Integer count, BigDecimal monthlySpending, BigDecimal annualSpending) {
            this.category = category;
            this.count = count;
            this.monthlySpending = monthlySpending;
            this.annualSpending = annualSpending;
        }

        // Getters and Setters

        public SubscriptionCategory getCategory() {
            return category;
        }

        public void setCategory(SubscriptionCategory category) {
            this.category = category;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public BigDecimal getMonthlySpending() {
            return monthlySpending;
        }

        public void setMonthlySpending(BigDecimal monthlySpending) {
            this.monthlySpending = monthlySpending;
        }

        public BigDecimal getAnnualSpending() {
            return annualSpending;
        }

        public void setAnnualSpending(BigDecimal annualSpending) {
            this.annualSpending = annualSpending;
        }

        public Double getPercentageOfTotal() {
            return percentageOfTotal;
        }

        public void setPercentageOfTotal(Double percentageOfTotal) {
            this.percentageOfTotal = percentageOfTotal;
        }
    }

    // Getters and Setters

    public Integer getTotalSubscriptions() {
        return totalSubscriptions;
    }

    public void setTotalSubscriptions(Integer totalSubscriptions) {
        this.totalSubscriptions = totalSubscriptions;
    }

    public Integer getActiveSubscriptions() {
        return activeSubscriptions;
    }

    public void setActiveSubscriptions(Integer activeSubscriptions) {
        this.activeSubscriptions = activeSubscriptions;
    }

    public Integer getCancelledSubscriptions() {
        return cancelledSubscriptions;
    }

    public void setCancelledSubscriptions(Integer cancelledSubscriptions) {
        this.cancelledSubscriptions = cancelledSubscriptions;
    }

    public Integer getUnusedSubscriptions() {
        return unusedSubscriptions;
    }

    public void setUnusedSubscriptions(Integer unusedSubscriptions) {
        this.unusedSubscriptions = unusedSubscriptions;
    }

    public BigDecimal getTotalMonthlyCost() {
        return totalMonthlyCost;
    }

    public void setTotalMonthlyCost(BigDecimal totalMonthlyCost) {
        this.totalMonthlyCost = totalMonthlyCost;
    }

    public BigDecimal getTotalAnnualCost() {
        return totalAnnualCost;
    }

    public void setTotalAnnualCost(BigDecimal totalAnnualCost) {
        this.totalAnnualCost = totalAnnualCost;
    }

    public BigDecimal getPotentialSavings() {
        return potentialSavings;
    }

    public void setPotentialSavings(BigDecimal potentialSavings) {
        this.potentialSavings = potentialSavings;
    }

    public Map<SubscriptionCategory, CategorySpending> getSpendingByCategory() {
        return spendingByCategory;
    }

    public void setSpendingByCategory(Map<SubscriptionCategory, CategorySpending> spendingByCategory) {
        this.spendingByCategory = spendingByCategory;
    }

    public List<SubscriptionDTO> getUpcomingRenewals() {
        return upcomingRenewals;
    }

    public void setUpcomingRenewals(List<SubscriptionDTO> upcomingRenewals) {
        this.upcomingRenewals = upcomingRenewals;
    }

    public List<SubscriptionDTO> getUnusedSubscriptionsList() {
        return unusedSubscriptionsList;
    }

    public void setUnusedSubscriptionsList(List<SubscriptionDTO> unusedSubscriptionsList) {
        this.unusedSubscriptionsList = unusedSubscriptionsList;
    }

    public Map<BillingCycle, Integer> getSubscriptionsByBillingCycle() {
        return subscriptionsByBillingCycle;
    }

    public void setSubscriptionsByBillingCycle(Map<BillingCycle, Integer> subscriptionsByBillingCycle) {
        this.subscriptionsByBillingCycle = subscriptionsByBillingCycle;
    }

    public SubscriptionDTO getMostExpensiveSubscription() {
        return mostExpensiveSubscription;
    }

    public void setMostExpensiveSubscription(SubscriptionDTO mostExpensiveSubscription) {
        this.mostExpensiveSubscription = mostExpensiveSubscription;
    }

    public String getTopCategory() {
        return topCategory;
    }

    public void setTopCategory(String topCategory) {
        this.topCategory = topCategory;
    }
}
