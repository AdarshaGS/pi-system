package com.investments.stocks.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PortfolioRebalancingService {
    
    public Map<String, Object> analyzePortfolio(Map<String, BigDecimal> currentAllocations, 
                                                Map<String, BigDecimal> targetAllocations,
                                                BigDecimal totalValue) {
        
        Map<String, Object> analysis = new HashMap<>();
        
        // Calculate current percentages
        Map<String, BigDecimal> currentPercentages = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : currentAllocations.entrySet()) {
            BigDecimal percentage = entry.getValue()
                .divide(totalValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            currentPercentages.put(entry.getKey(), percentage);
        }
        
        // Calculate deviations
        Map<String, BigDecimal> deviations = new HashMap<>();
        BigDecimal totalDeviation = BigDecimal.ZERO;
        
        for (Map.Entry<String, BigDecimal> entry : targetAllocations.entrySet()) {
            String asset = entry.getKey();
            BigDecimal target = entry.getValue();
            BigDecimal current = currentPercentages.getOrDefault(asset, BigDecimal.ZERO);
            BigDecimal deviation = current.subtract(target);
            deviations.put(asset, deviation);
            totalDeviation = totalDeviation.add(deviation.abs());
        }
        
        // Generate rebalancing suggestions
        List<Map<String, Object>> suggestions = generateRebalancingSuggestions(
            currentAllocations, targetAllocations, totalValue, deviations
        );
        
        // Calculate rebalancing urgency
        String urgency = calculateRebalancingUrgency(totalDeviation);
        
        analysis.put("currentAllocations", currentAllocations);
        analysis.put("currentPercentages", currentPercentages);
        analysis.put("targetAllocations", targetAllocations);
        analysis.put("deviations", deviations);
        analysis.put("totalDeviation", totalDeviation.setScale(2, RoundingMode.HALF_UP));
        analysis.put("needsRebalancing", totalDeviation.compareTo(BigDecimal.valueOf(5)) > 0);
        analysis.put("urgency", urgency);
        analysis.put("suggestions", suggestions);
        analysis.put("totalValue", totalValue);
        
        return analysis;
    }
    
    private List<Map<String, Object>> generateRebalancingSuggestions(
            Map<String, BigDecimal> currentAllocations,
            Map<String, BigDecimal> targetAllocations,
            BigDecimal totalValue,
            Map<String, BigDecimal> deviations) {
        
        List<Map<String, Object>> suggestions = new ArrayList<>();
        
        for (Map.Entry<String, BigDecimal> entry : deviations.entrySet()) {
            String asset = entry.getKey();
            BigDecimal deviation = entry.getValue();
            
            if (deviation.abs().compareTo(BigDecimal.valueOf(2)) > 0) {
                Map<String, Object> suggestion = new HashMap<>();
                suggestion.put("asset", asset);
                suggestion.put("action", deviation.compareTo(BigDecimal.ZERO) > 0 ? "SELL" : "BUY");
                suggestion.put("currentPercentage", currentAllocations.getOrDefault(asset, BigDecimal.ZERO)
                    .divide(totalValue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP));
                suggestion.put("targetPercentage", targetAllocations.getOrDefault(asset, BigDecimal.ZERO));
                suggestion.put("deviationPercentage", deviation.setScale(2, RoundingMode.HALF_UP));
                
                // Calculate amount to buy/sell
                BigDecimal targetValue = totalValue.multiply(
                    targetAllocations.getOrDefault(asset, BigDecimal.ZERO)
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                );
                BigDecimal currentValue = currentAllocations.getOrDefault(asset, BigDecimal.ZERO);
                BigDecimal amountToAdjust = targetValue.subtract(currentValue).abs();
                
                suggestion.put("amount", amountToAdjust.setScale(2, RoundingMode.HALF_UP));
                suggestion.put("priority", deviation.abs().compareTo(BigDecimal.valueOf(10)) > 0 ? "HIGH" : 
                                         deviation.abs().compareTo(BigDecimal.valueOf(5)) > 0 ? "MEDIUM" : "LOW");
                
                suggestions.add(suggestion);
            }
        }
        
        // Sort by priority
        suggestions.sort((a, b) -> {
            BigDecimal devA = new BigDecimal(a.get("deviationPercentage").toString()).abs();
            BigDecimal devB = new BigDecimal(b.get("deviationPercentage").toString()).abs();
            return devB.compareTo(devA);
        });
        
        return suggestions;
    }
    
    private String calculateRebalancingUrgency(BigDecimal totalDeviation) {
        if (totalDeviation.compareTo(BigDecimal.valueOf(20)) > 0) {
            return "CRITICAL";
        } else if (totalDeviation.compareTo(BigDecimal.valueOf(10)) > 0) {
            return "HIGH";
        } else if (totalDeviation.compareTo(BigDecimal.valueOf(5)) > 0) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
    
    public Map<String, Object> suggestOptimalAllocation(
            int age, 
            String riskTolerance, 
            int investmentHorizon,
            BigDecimal totalValue) {
        
        Map<String, BigDecimal> allocation = new HashMap<>();
        
        // Age-based rule of thumb: (100 - age) % in stocks
        int baseEquityPercentage = 100 - age;
        
        // Adjust based on risk tolerance
        int equityPercentage = switch (riskTolerance.toUpperCase()) {
            case "AGGRESSIVE" -> Math.min(baseEquityPercentage + 20, 95);
            case "MODERATE" -> baseEquityPercentage;
            case "CONSERVATIVE" -> Math.max(baseEquityPercentage - 20, 20);
            default -> baseEquityPercentage;
        };
        
        // Adjust based on investment horizon
        if (investmentHorizon > 20) {
            equityPercentage = Math.min(equityPercentage + 10, 95);
        } else if (investmentHorizon < 5) {
            equityPercentage = Math.max(equityPercentage - 15, 20);
        }
        
        int bondPercentage = 100 - equityPercentage;
        
        // Further breakdown
        allocation.put("domesticStocks", BigDecimal.valueOf(equityPercentage * 0.6));
        allocation.put("internationalStocks", BigDecimal.valueOf(equityPercentage * 0.3));
        allocation.put("emergingMarkets", BigDecimal.valueOf(equityPercentage * 0.1));
        allocation.put("bonds", BigDecimal.valueOf(bondPercentage * 0.7));
        allocation.put("cash", BigDecimal.valueOf(bondPercentage * 0.3));
        
        // Calculate dollar amounts
        Map<String, BigDecimal> dollarAmounts = allocation.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> totalValue.multiply(e.getValue().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                    .setScale(2, RoundingMode.HALF_UP)
            ));
        
        Map<String, Object> result = new HashMap<>();
        result.put("allocation", allocation);
        result.put("dollarAmounts", dollarAmounts);
        result.put("totalEquity", BigDecimal.valueOf(equityPercentage));
        result.put("totalBondsAndCash", BigDecimal.valueOf(bondPercentage));
        result.put("riskProfile", riskTolerance);
        result.put("rationale", generateAllocationRationale(age, riskTolerance, investmentHorizon));
        
        return result;
    }
    
    private List<String> generateAllocationRationale(int age, String riskTolerance, int investmentHorizon) {
        List<String> rationale = new ArrayList<>();
        
        rationale.add("Age-based allocation: At " + age + " years old, a balanced approach between growth and stability is recommended");
        rationale.add("Risk tolerance: " + riskTolerance + " profile suggests appropriate equity exposure");
        rationale.add("Investment horizon: " + investmentHorizon + " years allows for " + 
                     (investmentHorizon > 15 ? "more aggressive" : investmentHorizon > 7 ? "balanced" : "conservative") + 
                     " growth strategy");
        rationale.add("Diversification across domestic, international, and emerging markets reduces risk");
        rationale.add("Bond and cash allocation provides stability and liquidity");
        
        return rationale;
    }
    
    public Map<String, Object> calculateTaxEfficientRebalancing(
            List<Map<String, Object>> holdings,
            Map<String, BigDecimal> targetAllocations,
            BigDecimal totalValue) {
        
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> taxOptimizedSuggestions = new ArrayList<>();
        
        // Sort holdings by capital gains (sell losers first for tax harvesting)
        holdings.sort((a, b) -> {
            BigDecimal gainA = new BigDecimal(a.getOrDefault("capitalGain", "0").toString());
            BigDecimal gainB = new BigDecimal(b.getOrDefault("capitalGain", "0").toString());
            return gainA.compareTo(gainB);
        });
        
        for (Map<String, Object> holding : holdings) {
            String asset = holding.get("asset").toString();
            BigDecimal currentValue = new BigDecimal(holding.get("value").toString());
            BigDecimal capitalGain = new BigDecimal(holding.getOrDefault("capitalGain", "0").toString());
            boolean isLongTerm = Boolean.parseBoolean(holding.getOrDefault("isLongTerm", "false").toString());
            
            Map<String, Object> suggestion = new HashMap<>();
            suggestion.put("asset", asset);
            suggestion.put("currentValue", currentValue);
            suggestion.put("capitalGain", capitalGain);
            suggestion.put("isLongTerm", isLongTerm);
            
            if (capitalGain.compareTo(BigDecimal.ZERO) < 0) {
                suggestion.put("recommendation", "SELL_FOR_TAX_LOSS_HARVESTING");
                suggestion.put("priority", "HIGH");
            } else if (isLongTerm) {
                suggestion.put("recommendation", "CONSIDER_SELL_LONG_TERM_GAINS");
                suggestion.put("priority", "MEDIUM");
            } else {
                suggestion.put("recommendation", "HOLD_AVOID_SHORT_TERM_GAINS");
                suggestion.put("priority", "LOW");
            }
            
            taxOptimizedSuggestions.add(suggestion);
        }
        
        result.put("suggestions", taxOptimizedSuggestions);
        result.put("strategy", "Prioritize selling positions with losses for tax harvesting, " +
                               "followed by long-term gains, and avoid short-term capital gains");
        
        return result;
    }
}
