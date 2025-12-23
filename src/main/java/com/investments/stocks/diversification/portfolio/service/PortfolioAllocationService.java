package com.investments.stocks.diversification.portfolio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.investments.stocks.data.Stock;
import com.investments.stocks.diversification.portfolio.data.MarketCapAllocation;
import com.investments.stocks.diversification.portfolio.data.Portfolio;
import com.investments.stocks.diversification.portfolio.data.PortfolioAllocationResult;

@Service
public class PortfolioAllocationService {

    public PortfolioAllocationResult calculateAllocation(List<Portfolio> portfolios, Map<String, Stock> stockMap,
            Map<Long, String> sectorNameMap) {

        BigDecimal totalValue = BigDecimal.ZERO;
        Map<String, BigDecimal> sectorValueMap = new HashMap<>();
        BigDecimal largeCapValue = BigDecimal.ZERO;
        BigDecimal midCapValue = BigDecimal.ZERO;
        BigDecimal smallCapValue = BigDecimal.ZERO;

        for (Portfolio portfolio : portfolios) {
            Stock stock = stockMap.get(portfolio.getStockSymbol().toUpperCase());

            BigDecimal qty = BigDecimal.valueOf(portfolio.getQuantity());
            BigDecimal price = (stock != null && stock.getPrice() != null) ? BigDecimal.valueOf(stock.getPrice())
                    : (portfolio.getCurrentPrice() != null ? portfolio.getCurrentPrice()
                            : portfolio.getPurchasePrice());

            BigDecimal curVal = qty.multiply(price);
            totalValue = totalValue.add(curVal);

            // Sector
            String sectorName = "Unknown";
            if (stock != null && stock.getSectorId() != null) {
                sectorName = sectorNameMap.getOrDefault(stock.getSectorId(), "Unknown");
            }
            sectorValueMap.merge(sectorName, curVal, BigDecimal::add);

            // Market Cap
            if (stock != null && stock.getMarketCap() != null) {
                double mc = stock.getMarketCap();
                if (mc >= 20000) {
                    largeCapValue = largeCapValue.add(curVal);
                } else if (mc >= 5000) {
                    midCapValue = midCapValue.add(curVal);
                } else {
                    smallCapValue = smallCapValue.add(curVal);
                }
            } else {
                smallCapValue = smallCapValue.add(curVal);
            }
        }

        Map<String, BigDecimal> sectorAllocationPct = new HashMap<>();
        MarketCapAllocation mcAllocation = new MarketCapAllocation(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
            // Sector Pct
            for (Map.Entry<String, BigDecimal> entry : sectorValueMap.entrySet()) {
                BigDecimal pct = entry.getValue().divide(totalValue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                sectorAllocationPct.put(entry.getKey(), pct);
            }

            // Market Cap Pct
            mcAllocation = new MarketCapAllocation(
                    largeCapValue.divide(totalValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)),
                    midCapValue.divide(totalValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)),
                    smallCapValue.divide(totalValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
        }

        return PortfolioAllocationResult.builder()
                .sectorAllocation(sectorAllocationPct)
                .marketCapAllocation(mcAllocation)
                .build();
    }
}
