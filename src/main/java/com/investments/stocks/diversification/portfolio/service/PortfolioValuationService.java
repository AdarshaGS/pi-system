package com.investments.stocks.diversification.portfolio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.investments.stocks.data.Stock;
import com.investments.stocks.diversification.portfolio.data.Portfolio;
import com.investments.stocks.diversification.portfolio.data.PortfolioValuationResult;

import com.investments.stocks.data.PortfolioTransaction;
import com.common.utils.XirrCalculator;
import java.util.ArrayList;

@Service
public class PortfolioValuationService {

    public PortfolioValuationResult calculateValuation(List<Portfolio> portfolios, Map<String, Stock> stockMap,
            List<PortfolioTransaction> transactions) {
        BigDecimal totalInvestment = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;
        BigDecimal unrealizedGain = BigDecimal.ZERO;

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
            unrealizedGain = unrealizedGain.add(curVal.subtract(investment));
        }

        // Calculate Realized Gain from transactions
        BigDecimal realizedGain = transactions.stream()
                .filter(t -> t.getTransactionType() == PortfolioTransaction.TransactionType.SELL)
                .map(t -> t.getRealizedGain() != null ? t.getRealizedGain() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Include Dividends in realized gain? Usually yes.
        BigDecimal totalDividends = transactions.stream()
                .filter(t -> t.getTransactionType() == PortfolioTransaction.TransactionType.DIVIDEND)
                .map(t -> t.getTotalAmount() != null ? t.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        realizedGain = realizedGain.add(totalDividends);

        BigDecimal totalProfitLoss = unrealizedGain.add(realizedGain);
        BigDecimal totalProfitLossPercentage = BigDecimal.ZERO;

        if (totalInvestment.compareTo(BigDecimal.ZERO) > 0) {
            totalProfitLossPercentage = totalProfitLoss.divide(totalInvestment, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // Calculate XIRR
        double xirrValue = 0.0;
        if (!transactions.isEmpty()) {
            List<XirrCalculator.CashFlow> cashFlows = new ArrayList<>();

            // 1. All historical transactions (Buy = negative cash flow, Sell/Dividend =
            // positive)
            for (PortfolioTransaction t : transactions) {
                double amount = 0.0;
                if (t.getTransactionType() == PortfolioTransaction.TransactionType.BUY) {
                    amount = -t.getTotalAmount().doubleValue();
                } else if (t.getTransactionType() == PortfolioTransaction.TransactionType.SELL ||
                        t.getTransactionType() == PortfolioTransaction.TransactionType.DIVIDEND) {
                    amount = t.getTotalAmount().doubleValue();
                }

                if (amount != 0) {
                    cashFlows.add(new XirrCalculator.CashFlow(t.getTransactionDate(), amount));
                }
            }

            // 2. Add current value as a final positive cash flow (hypothetical exit today)
            if (currentValue.compareTo(BigDecimal.ZERO) > 0) {
                cashFlows.add(new XirrCalculator.CashFlow(java.time.LocalDate.now(), currentValue.doubleValue()));
            }

            if (cashFlows.size() >= 2) {
                try {
                    xirrValue = XirrCalculator.calculate(cashFlows);
                } catch (Exception e) {
                    // Fallback to 0 if calculation fails
                }
            }
        }

        return PortfolioValuationResult.builder()
                .totalInvestment(totalInvestment)
                .currentValue(currentValue)
                .totalProfitLoss(totalProfitLoss)
                .totalProfitLossPercentage(totalProfitLossPercentage)
                .realizedGain(realizedGain)
                .unrealizedGain(unrealizedGain)
                .xirr(BigDecimal.valueOf(xirrValue).setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    public PortfolioValuationResult calculateValuation(List<Portfolio> portfolios, Map<String, Stock> stockMap) {
        return calculateValuation(portfolios, stockMap, List.of());
    }
}
