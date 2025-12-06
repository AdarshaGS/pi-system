package com.stocks.diversification.portfolio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.stocks.data.Stock;
import com.stocks.diversification.portfolio.data.AnalysisInsight;
import com.stocks.diversification.portfolio.data.MarketCapAllocation;
import com.stocks.diversification.portfolio.data.Portfolio;
import com.stocks.diversification.portfolio.data.PortfolioDTOResponse;
import com.stocks.diversification.portfolio.repo.PortfolioRepository;
import com.stocks.diversification.sectors.data.Sector;
import com.stocks.diversification.sectors.repo.SectorRepository;
import com.stocks.repo.StockRepository;

@Service
public class PortfolioReadPlatformServiceImpl implements PortfolioReadPlatformService {

    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;
    private final SectorRepository sectorRepository;
    private final PortfolioAnalyzerEngine portfolioAnalyzerEngine;

    public PortfolioReadPlatformServiceImpl(PortfolioRepository portfolioRepository,
            StockRepository stockRepository,
            SectorRepository sectorRepository,
            PortfolioAnalyzerEngine portfolioAnalyzerEngine) {
        this.portfolioRepository = portfolioRepository;
        this.stockRepository = stockRepository;
        this.sectorRepository = sectorRepository;
        this.portfolioAnalyzerEngine = portfolioAnalyzerEngine;
    }

    @Override
    public PortfolioDTOResponse getPortfolioSummary(Long userId) {
        return getDiversificationScore(userId);
    }

    @Override
    public PortfolioDTOResponse getDiversificationScore(Long userId) {
        List<Portfolio> userPortfolios = portfolioRepository.findByUserId(userId);
        return generatePortfolioResponse(userPortfolios);
    }

    private PortfolioDTOResponse generatePortfolioResponse(List<Portfolio> userPortfolios) {
        if (userPortfolios.isEmpty()) {
            return PortfolioDTOResponse.builder()
                    .score(0)
                    .assessment("No Data")
                    .recommendations(List.of("Consider adding more diverse assets"))
                    .totalInvestment(BigDecimal.ZERO)
                    .currentValue(BigDecimal.ZERO)
                    .totalProfitLoss(BigDecimal.ZERO)
                    .totalProfitLossPercentage(BigDecimal.ZERO)
                    .sectorAllocation(new HashMap<>())
                    .healthScore(0)
                    .insights(new ArrayList<>())
                    .build();
        }

        List<String> stockSymbols = userPortfolios.stream()
                .map(Portfolio::getStockSymbol)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Stock> stockMap = stockRepository.findBySymbolIn(stockSymbols).stream()
                .collect(Collectors.toMap(Stock::getSymbol, Function.identity()));

        Set<Long> sectorIds = stockMap.values().stream()
                .map(Stock::getSectorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> sectorNameMap = sectorRepository.findAllById(sectorIds).stream()
                .collect(Collectors.toMap(Sector::getId, Sector::getName));

        BigDecimal totalInvestment = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;
        BigDecimal largeCapValue = BigDecimal.ZERO;
        BigDecimal midCapValue = BigDecimal.ZERO;
        BigDecimal smallCapValue = BigDecimal.ZERO;

        Map<String, BigDecimal> sectorValueMap = new HashMap<>();

        for (Portfolio portfolio : userPortfolios) {
            Stock stock = stockMap.get(portfolio.getStockSymbol());

            BigDecimal investment = portfolio.getPurchasePrice().multiply(BigDecimal.valueOf(portfolio.getQuantity()));
            BigDecimal curValue = BigDecimal.ZERO;

            if (stock != null && stock.getPrice() != null) {
                curValue = BigDecimal.valueOf(stock.getPrice()).multiply(BigDecimal.valueOf(portfolio.getQuantity()));
            } else if (portfolio.getCurrentPrice() != null) {
                curValue = portfolio.getCurrentPrice().multiply(BigDecimal.valueOf(portfolio.getQuantity()));
            } else {
                curValue = investment; // Fallback
            }

            totalInvestment = totalInvestment.add(investment);
            currentValue = currentValue.add(curValue);

            String sectorName = "Others";
            if (stock != null && stock.getSectorId() != null) {
                sectorName = sectorNameMap.getOrDefault(stock.getSectorId(), "Others");
            }
            sectorValueMap.merge(sectorName, curValue, BigDecimal::add);

            // Market Cap Logic
            if (stock != null && stock.getMarketCap() != null) {
                double mc = stock.getMarketCap(); // In Cr
                if (mc >= 20000) {
                    largeCapValue = largeCapValue.add(curValue);
                } else if (mc >= 5000) {
                    midCapValue = midCapValue.add(curValue);
                } else {
                    smallCapValue = smallCapValue.add(curValue);
                }
            } else {
                // Default to Small Cap if unknown? Or explicit unclassified?
                // Let's treat as Small Cap for now as per plan
                smallCapValue = smallCapValue.add(curValue);
            }
        }

        BigDecimal totalProfitLoss = currentValue.subtract(totalInvestment);
        BigDecimal totalProfitLossPercentage = totalInvestment.compareTo(BigDecimal.ZERO) != 0
                ? totalProfitLoss.divide(totalInvestment, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        Map<String, BigDecimal> sectorAllocationBigDecimal = new HashMap<>();
        // Calculate Sector Percentages
        for (Map.Entry<String, BigDecimal> entry : sectorValueMap.entrySet()) {
            if (currentValue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percentage = entry.getValue().divide(currentValue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                sectorAllocationBigDecimal.put(entry.getKey(), percentage);
            } else {
                sectorAllocationBigDecimal.put(entry.getKey(), BigDecimal.ZERO);
            }
        }

        // Convert to Map<String, BigDecimal> for legacy score calculation and DTO
        Map<String, BigDecimal> sectorAllocation = sectorAllocationBigDecimal;

        // Calculate Market Cap Percentages
        MarketCapAllocation mcAllocation = new MarketCapAllocation(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        if (currentValue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal lcPct = largeCapValue.divide(currentValue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            BigDecimal mcPct = midCapValue.divide(currentValue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            BigDecimal scPct = smallCapValue.divide(currentValue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            mcAllocation = new MarketCapAllocation(lcPct, mcPct, scPct);
        }

        // Use Rule Engine
        List<AnalysisInsight> insights = portfolioAnalyzerEngine.analyze(userPortfolios, stockMap, sectorNameMap);
        int healthScore = portfolioAnalyzerEngine.calculateHealthScore(insights);

        // Calculate Diversification Score (Legacy)
        int diversificationScore = calculateScore(sectorAllocation);
        List<String> recommendations = generateRecommendations(sectorAllocation, diversificationScore);

        return PortfolioDTOResponse.builder()
                .totalInvestment(totalInvestment)
                .currentValue(currentValue)
                .totalProfitLoss(currentValue.subtract(totalInvestment))
                .totalProfitLossPercentage(totalProfitLossPercentage)
                .sectorAllocation(sectorAllocation)
                .score(diversificationScore)
                .assessment(diversificationScore > 70 ? "Good" : "Needs Improvement")
                .recommendations(recommendations)
                .healthScore(healthScore)
                .insights(insights)
                .marketCapAllocation(mcAllocation)
                .build();
    }

    private int calculateScore(Map<String, BigDecimal> sectorAllocation) {
        if (sectorAllocation.isEmpty())
            return 0;
        int score = 100;
        int sectorCount = sectorAllocation.size();

        if (sectorCount < 3)
            score -= 30;
        else if (sectorCount < 5)
            score -= 10;

        for (BigDecimal pct : sectorAllocation.values()) {
            if (pct.doubleValue() > 25.0)
                score -= 10;
            if (pct.doubleValue() > 50.0)
                score -= 20;
        }

        return Math.max(0, score);
    }


    private List<String> generateRecommendations(Map<String, BigDecimal> sectorAllocation, int score) {
        List<String> recs = new ArrayList<>();
        if (score < 60)
            recs.add("Consider diversifying into more sectors.");
        for (Map.Entry<String, BigDecimal> entry : sectorAllocation.entrySet()) {
            if (entry.getValue().doubleValue() > 30.0) {
                recs.add("High exposure to " + entry.getKey() + " sector (" + entry.getValue()
                        + "%). Consider reducing.");
            }
        }
        if (sectorAllocation.size() < 3)
            recs.add("You have investments in few sectors. Look for opportunities in new industries.");
        if (recs.isEmpty())
            recs.add("Your portfolio looks well diversified!");
        return recs;
    }
}
