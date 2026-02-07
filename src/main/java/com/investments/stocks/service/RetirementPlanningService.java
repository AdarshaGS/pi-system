package com.investments.stocks.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class RetirementPlanningService {
    
    public Map<String, Object> calculateRetirementPlan(
            int currentAge,
            int retirementAge,
            BigDecimal currentSavings,
            BigDecimal monthlyContribution,
            BigDecimal expectedReturn,
            BigDecimal inflationRate,
            BigDecimal desiredMonthlyIncome,
            int lifeExpectancy) {
        
        int yearsToRetirement = retirementAge - currentAge;
        int yearsInRetirement = lifeExpectancy - retirementAge;
        
        // Calculate future value of current savings and contributions
        BigDecimal monthlyReturn = expectedReturn.divide(BigDecimal.valueOf(1200), 6, RoundingMode.HALF_UP);
        int totalMonths = yearsToRetirement * 12;
        
        // Future value of current savings: FV = PV * (1+r)^n
        BigDecimal futureValueOfSavings = currentSavings.multiply(
            BigDecimal.ONE.add(monthlyReturn).pow(totalMonths)
        );
        
        // Future value of monthly contributions (annuity): FV = PMT * [((1+r)^n - 1) / r]
        BigDecimal futureValueOfContributions = BigDecimal.ZERO;
        if (monthlyReturn.compareTo(BigDecimal.ZERO) > 0) {
            futureValueOfContributions = monthlyContribution.multiply(
                BigDecimal.ONE.add(monthlyReturn).pow(totalMonths)
                    .subtract(BigDecimal.ONE)
                    .divide(monthlyReturn, 6, RoundingMode.HALF_UP)
            );
        }
        
        BigDecimal totalRetirementFund = futureValueOfSavings.add(futureValueOfContributions);
        
        // Adjust desired income for inflation
        BigDecimal inflationFactor = BigDecimal.ONE.add(
            inflationRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
        ).pow(yearsToRetirement);
        BigDecimal adjustedMonthlyIncome = desiredMonthlyIncome.multiply(inflationFactor);
        
        // Calculate required retirement fund using 4% rule (adjusted)
        BigDecimal annualIncome = adjustedMonthlyIncome.multiply(BigDecimal.valueOf(12));
        BigDecimal requiredRetirementFund = annualIncome.multiply(BigDecimal.valueOf(25)); // 4% rule
        
        // Calculate shortfall or surplus
        BigDecimal shortfall = requiredRetirementFund.subtract(totalRetirementFund);
        
        // Calculate required additional monthly contribution to meet goal
        BigDecimal additionalContribution = BigDecimal.ZERO;
        if (shortfall.compareTo(BigDecimal.ZERO) > 0 && monthlyReturn.compareTo(BigDecimal.ZERO) > 0) {
            additionalContribution = shortfall.divide(
                BigDecimal.ONE.add(monthlyReturn).pow(totalMonths)
                    .subtract(BigDecimal.ONE)
                    .divide(monthlyReturn, 6, RoundingMode.HALF_UP),
                2, RoundingMode.HALF_UP
            );
        }
        
        // Build response
        Map<String, Object> result = new HashMap<>();
        result.put("yearsToRetirement", yearsToRetirement);
        result.put("yearsInRetirement", yearsInRetirement);
        result.put("projectedRetirementFund", totalRetirementFund.setScale(2, RoundingMode.HALF_UP));
        result.put("requiredRetirementFund", requiredRetirementFund.setScale(2, RoundingMode.HALF_UP));
        result.put("shortfall", shortfall.setScale(2, RoundingMode.HALF_UP));
        result.put("onTrack", shortfall.compareTo(BigDecimal.ZERO) <= 0);
        result.put("adjustedMonthlyIncome", adjustedMonthlyIncome.setScale(2, RoundingMode.HALF_UP));
        result.put("additionalMonthlyContributionNeeded", additionalContribution.setScale(2, RoundingMode.HALF_UP));
        result.put("savingsRate", calculateSavingsProgress(totalRetirementFund, requiredRetirementFund));
        
        // Year-by-year projection
        List<Map<String, Object>> yearlyProjection = new ArrayList<>();
        BigDecimal cumulativeSavings = currentSavings;
        
        for (int year = 1; year <= Math.min(yearsToRetirement, 30); year++) {
            for (int month = 1; month <= 12; month++) {
                cumulativeSavings = cumulativeSavings
                    .multiply(BigDecimal.ONE.add(monthlyReturn))
                    .add(monthlyContribution);
            }
            
            Map<String, Object> yearData = new HashMap<>();
            yearData.put("year", currentAge + year);
            yearData.put("age", currentAge + year);
            yearData.put("balance", cumulativeSavings.setScale(2, RoundingMode.HALF_UP));
            yearlyProjection.add(yearData);
        }
        
        result.put("yearlyProjection", yearlyProjection);
        result.put("recommendations", generateRetirementRecommendations(shortfall, additionalContribution, yearsToRetirement));
        
        return result;
    }
    
    private BigDecimal calculateSavingsProgress(BigDecimal current, BigDecimal required) {
        if (required.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }
        return current.divide(required, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .min(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP);
    }
    
    private List<String> generateRetirementRecommendations(BigDecimal shortfall, BigDecimal additionalContribution, int yearsToRetirement) {
        List<String> recommendations = new ArrayList<>();
        
        if (shortfall.compareTo(BigDecimal.ZERO) > 0) {
            recommendations.add("Increase monthly contributions by $" + additionalContribution + " to meet retirement goal");
            recommendations.add("Consider delaying retirement by 2-3 years to build more savings");
            recommendations.add("Review and reduce unnecessary expenses to increase savings rate");
            recommendations.add("Explore higher-yielding investment options appropriate for your risk tolerance");
        } else {
            recommendations.add("You're on track to meet your retirement goals!");
            recommendations.add("Consider contributing extra to build a larger safety cushion");
            recommendations.add("Review your investment allocation annually");
            recommendations.add("Plan for healthcare costs in retirement");
        }
        
        if (yearsToRetirement > 20) {
            recommendations.add("With " + yearsToRetirement + " years to retirement, consider more aggressive growth investments");
        } else if (yearsToRetirement < 10) {
            recommendations.add("With less than 10 years to retirement, consider shifting to more conservative investments");
        }
        
        return recommendations;
    }
    
    public Map<String, Object> calculateSafWithdrawalRate(BigDecimal portfolioValue, int withdrawalYears) {
        // 4% rule and adjustments
        BigDecimal safeWithdrawalRate = BigDecimal.valueOf(4.0);
        BigDecimal annualWithdrawal = portfolioValue.multiply(safeWithdrawalRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        BigDecimal monthlyWithdrawal = annualWithdrawal.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        
        Map<String, Object> result = new HashMap<>();
        result.put("safeWithdrawalRate", safeWithdrawalRate);
        result.put("annualWithdrawal", annualWithdrawal.setScale(2, RoundingMode.HALF_UP));
        result.put("monthlyWithdrawal", monthlyWithdrawal.setScale(2, RoundingMode.HALF_UP));
        result.put("portfolioValue", portfolioValue);
        result.put("estimatedDuration", withdrawalYears);
        
        return result;
    }
}
