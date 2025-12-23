package com.investments.stocks.diversification.portfolio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.investments.stocks.data.Stock;
import com.investments.stocks.diversification.portfolio.data.Portfolio;
import com.investments.stocks.diversification.portfolio.data.PortfolioValuationResult;

@Service
public class PortfolioValuationService {

    public PortfolioValuationResult calculateValuation(List<Portfolio> portfolios, Map<String, Stock> stockMap) {
        BigDecimal totalInvestment = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;

        for (Portfolio portfolio : portfolios) {
            Stock stock = stockMap.get(portfolio.getStockSymbol().toUpperCase());

            BigDecimal qty = BigDecimal.valueOf(portfolio.getQuantity());
            BigDecimal buyPrice = portfolio.getPurchasePrice();

            BigDecimal currentPrice = buyPrice; // Fallback
            if (stock != null && stock.getPrice() != null) {
                currentPrice = BigDecimal.valueOf(stock.getPrice());
            } else if (portfolio.getCurrentPrice() != null) {
                currentPrice = portfolio.getCurrentPrice();
            }

            BigDecimal investment = qty.multiply(buyPrice);
            BigDecimal curVal = qty.multiply(currentPrice);

            totalInvestment = totalInvestment.add(investment);
            currentValue = currentValue.add(curVal);
        }

        BigDecimal totalProfitLoss = currentValue.subtract(totalInvestment);
        BigDecimal totalProfitLossPercentage = BigDecimal.ZERO;

        if (totalInvestment.compareTo(BigDecimal.ZERO) > 0) {
            totalProfitLossPercentage = totalProfitLoss.divide(totalInvestment, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return PortfolioValuationResult.builder()
                .totalInvestment(totalInvestment)
                .currentValue(currentValue)
                .totalProfitLoss(totalProfitLoss)
                .totalProfitLossPercentage(totalProfitLossPercentage)
                .build();
    }
}
