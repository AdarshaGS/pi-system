package com.investments.stocks.diversification.portfolio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import com.investments.stocks.data.Stock;
import com.investments.stocks.diversification.portfolio.data.MarketCapAllocation;
import com.investments.stocks.diversification.portfolio.data.Portfolio;
import com.investments.stocks.diversification.portfolio.data.PortfolioAllocationResult;
import com.investments.stocks.diversification.portfolio.data.SectorAllocation;

@Service
@Slf4j
public class PortfolioAllocationService {

    // Market cap thresholds in INR Crores (as per SEBI classification)
    private static final double LARGE_CAP_THRESHOLD = 20000.0; // ≥₹20,000 Cr
    private static final double MID_CAP_THRESHOLD = 5000.0;    // ≥₹5,000 Cr
    
    private static final int PERCENTAGE_SCALE = 2; // 2 decimal places for percentages
    private static final String UNCLASSIFIED_SECTOR = "Unclassified";

    @Transactional(readOnly = true)
    public PortfolioAllocationResult calculateAllocation(List<Portfolio> portfolios, 
            Map<String, Stock> stockMap, Map<Long, String> sectorNameMap) {
        
        if (portfolios == null || portfolios.isEmpty()) {
            return createEmptyResult();
        }

        BigDecimal totalValue = BigDecimal.ZERO;
        Map<String, BigDecimal> sectorValueMap = new HashMap<>();
        BigDecimal largeCapValue = BigDecimal.ZERO;
        BigDecimal midCapValue = BigDecimal.ZERO;
        BigDecimal smallCapValue = BigDecimal.ZERO;
        BigDecimal unclassifiedValue = BigDecimal.ZERO;

        for (Portfolio portfolio : portfolios) {
            Stock stock = stockMap.get(portfolio.getStockSymbol().toUpperCase());
            BigDecimal currentValue = calculateHoldingValue(stock, portfolio);
            
            totalValue = totalValue.add(currentValue);
            
            // Sector allocation
            String sectorName = determineSectorName(stock, portfolio, sectorNameMap);
            sectorValueMap.merge(sectorName, currentValue, BigDecimal::add);
            
            // Market cap allocation
            MarketCapCategory category = classifyMarketCap(stock, portfolio);
            switch (category) {
                case LARGE_CAP:
                    largeCapValue = largeCapValue.add(currentValue);
                    break;
                case MID_CAP:
                    midCapValue = midCapValue.add(currentValue);
                    break;
                case SMALL_CAP:
                    smallCapValue = smallCapValue.add(currentValue);
                    break;
                case UNCLASSIFIED:
                    unclassifiedValue = unclassifiedValue.add(currentValue);
                    break;
            }
        }

        return buildAllocationResult(totalValue, sectorValueMap, 
            largeCapValue, midCapValue, smallCapValue, unclassifiedValue);
    }

    private BigDecimal calculateHoldingValue(Stock stock, Portfolio portfolio) {
        BigDecimal quantity = BigDecimal.valueOf(portfolio.getQuantity());
        BigDecimal price = determineStockPrice(stock, portfolio);
        return quantity.multiply(price);
    }

    private BigDecimal determineStockPrice(Stock stock, Portfolio portfolio) {
        // Priority: Live price → Current price → Purchase price
        if (stock != null && stock.getPrice() != null) {
            return BigDecimal.valueOf(stock.getPrice());
        }
        if (portfolio.getCurrentPrice() != null) {
            return portfolio.getCurrentPrice();
        }
        return portfolio.getPurchasePrice();
    }

    private String determineSectorName(Stock stock, Portfolio portfolio, 
            Map<Long, String> sectorNameMap) {
        if (stock == null || stock.getSectorId() == null) {
            log.warn("Missing sector data for stock: {}", portfolio.getStockSymbol());
            return UNCLASSIFIED_SECTOR;
        }
        return sectorNameMap.getOrDefault(stock.getSectorId(), UNCLASSIFIED_SECTOR);
    }

    private MarketCapCategory classifyMarketCap(Stock stock, Portfolio portfolio) {
        if (stock == null || stock.getMarketCap() == null) {
            log.warn("Missing market cap data for stock: {}", portfolio.getStockSymbol());
            return MarketCapCategory.UNCLASSIFIED;
        }
        
        double marketCap = stock.getMarketCap();
        if (marketCap >= LARGE_CAP_THRESHOLD) {
            return MarketCapCategory.LARGE_CAP;
        } else if (marketCap >= MID_CAP_THRESHOLD) {
            return MarketCapCategory.MID_CAP;
        } else {
            return MarketCapCategory.SMALL_CAP;
        }
    }

    private PortfolioAllocationResult buildAllocationResult(BigDecimal totalValue,
            Map<String, BigDecimal> sectorValueMap, BigDecimal largeCapValue,
            BigDecimal midCapValue, BigDecimal smallCapValue, BigDecimal unclassifiedValue) {
        
        if (totalValue.compareTo(BigDecimal.ZERO) == 0) {
            return createEmptyResult();
        }

        // Calculate sector percentages
        Map<String, BigDecimal> sectorAllocationPct = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : sectorValueMap.entrySet()) {
            BigDecimal percentage = calculatePercentage(entry.getValue(), totalValue);
            sectorAllocationPct.put(entry.getKey(), percentage);
        }

        // Calculate market cap percentages
        MarketCapAllocation mcAllocation = new MarketCapAllocation(
            calculatePercentage(largeCapValue, totalValue),
            calculatePercentage(midCapValue, totalValue),
            calculatePercentage(smallCapValue, totalValue)
        );

        return PortfolioAllocationResult.builder()
                .sectorAllocation(SectorAllocation.builder().sectors(sectorAllocationPct).build())
                .marketCapAllocation(mcAllocation)
                .build();
    }

    private BigDecimal calculatePercentage(BigDecimal value, BigDecimal total) {
        return value.divide(total, PERCENTAGE_SCALE + 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);
    }

    private PortfolioAllocationResult createEmptyResult() {
        return PortfolioAllocationResult.builder()
                .sectorAllocation(SectorAllocation.builder().sectors(new HashMap<>()).build())
                .marketCapAllocation(new MarketCapAllocation(
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO))
                .build();
    }

    private enum MarketCapCategory {
        LARGE_CAP, MID_CAP, SMALL_CAP, UNCLASSIFIED
    }
}
