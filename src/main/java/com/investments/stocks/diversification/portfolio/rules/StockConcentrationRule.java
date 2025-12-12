package com.investments.stocks.diversification.portfolio.rules;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.investments.stocks.data.Stock;
import com.investments.stocks.diversification.portfolio.data.AnalysisInsight;
import com.investments.stocks.diversification.portfolio.data.Portfolio;

@Component
public class StockConcentrationRule implements PortfolioAnalysisRule {

    private static final double CRITICAL_CONCENTRATION_PERCENT = 20.0;
    private static final double WARNING_CONCENTRATION_PERCENT = 10.0;

    @Override
    public List<AnalysisInsight> evaluate(List<Portfolio> portfolios, Map<String, Stock> stockData,
            Map<Long, String> sectorMap) {
        List<AnalysisInsight> insights = new ArrayList<>();
        BigDecimal totalPortfolioValue = BigDecimal.ZERO;

        // Calculate total value
        for (Portfolio p : portfolios) {
            BigDecimal price = p.getCurrentPrice() != null ? p.getCurrentPrice() : p.getPurchasePrice();
            totalPortfolioValue = totalPortfolioValue.add(price.multiply(BigDecimal.valueOf(p.getQuantity())));
        }

        if (totalPortfolioValue.compareTo(BigDecimal.ZERO) == 0) {
            return insights;
        }

        // Check each stock
        for (Portfolio p : portfolios) {
            BigDecimal price = p.getCurrentPrice() != null ? p.getCurrentPrice() : p.getPurchasePrice();
            BigDecimal stockValue = price.multiply(BigDecimal.valueOf(p.getQuantity()));

            double concentration = stockValue.divide(totalPortfolioValue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();

            if (concentration > CRITICAL_CONCENTRATION_PERCENT) {
                insights.add(AnalysisInsight.builder()
                        .type(AnalysisInsight.InsightType.CRITICAL)
                        .category(AnalysisInsight.InsightCategory.DIVERSIFICATION)
                        .message(String.format(
                                "High concentration in %s (%.1f%%). Risk of significant loss if this stock crashes.",
                                p.getStockSymbol(), concentration))
                        .recommendedAction(String.format("Reduce exposure to %s to below 10%%.", p.getStockSymbol()))
                        .build());
            } else if (concentration > WARNING_CONCENTRATION_PERCENT) {
                insights.add(AnalysisInsight.builder()
                        .type(AnalysisInsight.InsightType.WARNING)
                        .category(AnalysisInsight.InsightCategory.DIVERSIFICATION)
                        .message(String.format("Significant exposure to %s (%.1f%%).", p.getStockSymbol(),
                                concentration))
                        .recommendedAction("Consider rebalancing if you are not strictly bullish.")
                        .build());
            }
        }

        return insights;
    }
}
