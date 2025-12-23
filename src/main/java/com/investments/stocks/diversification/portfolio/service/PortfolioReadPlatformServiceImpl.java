package com.investments.stocks.diversification.portfolio.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.investments.stocks.data.Stock;
import com.investments.stocks.diversification.portfolio.data.AnalysisInsight;
import com.investments.stocks.diversification.portfolio.data.DataFreshness;
import com.investments.stocks.diversification.portfolio.data.Portfolio;
import com.investments.stocks.diversification.portfolio.data.PortfolioAllocationResult;
import com.investments.stocks.diversification.portfolio.data.PortfolioDTOResponse;
import com.investments.stocks.diversification.portfolio.data.PortfolioInsightsDTO;
import com.investments.stocks.diversification.portfolio.data.PortfolioScoringResult;
import com.investments.stocks.diversification.portfolio.data.PortfolioValuationResult;
import com.investments.stocks.diversification.portfolio.data.RiskAnalysisResult;
import com.investments.stocks.diversification.portfolio.data.RiskSummary;
import com.investments.stocks.diversification.portfolio.repo.PortfolioRepository;
import com.investments.stocks.diversification.sectors.data.Sector;
import com.investments.stocks.diversification.sectors.repo.SectorRepository;
import com.investments.stocks.repo.StockRepository;
import com.savings.service.SavingsAccountService;
import com.savings.service.FixedDepositService;
import com.savings.service.RecurringDepositService;
import com.savings.data.SavingsAccountDTO;
import com.savings.data.FixedDepositDTO;
import com.savings.data.RecurringDepositDTO;
import com.loan.service.LoanService;
import com.protection.insurance.service.InsuranceService;

@Service
public class PortfolioReadPlatformServiceImpl implements PortfolioReadPlatformService {

    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;
    private final SectorRepository sectorRepository;

    private final PortfolioValuationService valuationService;
    private final PortfolioAllocationService allocationService;
    private final PortfolioRiskEvaluationService riskEvaluationService;
    private final PortfolioScoringService scoringService;
    private final PortfolioInsightService insightService;

    public PortfolioReadPlatformServiceImpl(
            PortfolioRepository portfolioRepository,
            StockRepository stockRepository,
            SectorRepository sectorRepository,
            PortfolioValuationService valuationService,
            PortfolioAllocationService allocationService,
            PortfolioRiskEvaluationService riskEvaluationService,
            PortfolioScoringService scoringService,
            PortfolioInsightService insightService) {
        this.portfolioRepository = portfolioRepository;
        this.stockRepository = stockRepository;
        this.sectorRepository = sectorRepository;
        this.valuationService = valuationService;
        this.allocationService = allocationService;
        this.riskEvaluationService = riskEvaluationService;
        this.scoringService = scoringService;
        this.insightService = insightService;
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
                    .recommendations(List.of("Start investing to see your portfolio analysis."))
                    .totalInvestment(BigDecimal.ZERO)
                    .currentValue(BigDecimal.ZERO)
                    .totalProfitLoss(BigDecimal.ZERO)
                    .totalProfitLossPercentage(BigDecimal.ZERO)
                    .sectorAllocation(new HashMap<>())
                    .insights(PortfolioInsightsDTO.builder()
                            .critical(new ArrayList<>())
                            .warning(new ArrayList<>())
                            .info(new ArrayList<>())
                            .build())
                    .riskSummary(RiskSummary.builder().build())
                    .build();
        }

        // 1. Fetch Stocks and Sectors
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

        // 2. Valuation
        PortfolioValuationResult valuation = valuationService.calculateValuation(userPortfolios, stockMap);

        // 3. Allocation
        PortfolioAllocationResult allocation = allocationService.calculateAllocation(userPortfolios, stockMap,
                sectorNameMap);

        // 4. Risk Evaluation
        RiskAnalysisResult riskResult = riskEvaluationService.evaluateRisks(
                userPortfolios,
                stockMap,
                valuation.getCurrentValue(),
                allocation.getSectorAllocation(),
                allocation.getMarketCapAllocation().getSmallCapPercentage());

        // 5. Scoring
        PortfolioScoringResult scoring = scoringService.calculateScore(riskResult);

        // 6. Insights & Risk Summary
        PortfolioInsightsDTO insights = insightService.groupInsights(riskResult.getInsights());
        RiskSummary riskSummary = insightService.calculateRiskSummary(riskResult.getInsights());

        // 7. Data Freshness
        DataFreshness freshness = DataFreshness.builder()
                .priceLastUpdatedAt(LocalDateTime.now().toString())
                .isStale(false)
                .build();

        // Collect recommendations from insights for backward compatibility or display
        List<String> recommendations = riskResult.getInsights().stream()
                .map(AnalysisInsight::getRecommendedAction)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (recommendations.isEmpty()) {
            recommendations.add("Your portfolio is looking good!");
        }

        // Extended aggregates
        BigDecimal savingsTotal = BigDecimal.ZERO;
        Long uid = userPortfolios.isEmpty() ? null : userPortfolios.get(0).getUserId();
        
        // Cash savings
        try {
            List<SavingsAccountDTO> savings = savingsAccountService.getAllSavingsAccounts(uid);
            if (savings != null) {
                savingsTotal = savings.stream()
                    .map(SavingsAccountDTO::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
        } catch (Exception ignored) {}
        
        // Fixed Deposits
        try {
            if (uid != null) {
                List<FixedDepositDTO> fds = fixedDepositService.getAllFixedDeposits(uid);
                if (fds != null) {
                    BigDecimal fdValue = fds.stream()
                        .map(fd -> fd.getMaturityAmount() != null ? fd.getMaturityAmount() : fd.getPrincipalAmount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    savingsTotal = savingsTotal.add(fdValue);
                }
            }
        } catch (Exception ignored) {}
        
        // Recurring Deposits
        try {
            if (uid != null) {
                List<RecurringDepositDTO> rds = recurringDepositService.getAllRecurringDeposits(uid);
                if (rds != null) {
                    BigDecimal rdValue = rds.stream()
                        .map(rd -> rd.getMaturityAmount() != null ? rd.getMaturityAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    savingsTotal = savingsTotal.add(rdValue);
                }
            }
        } catch (Exception ignored) {}

        BigDecimal loansOutstanding = BigDecimal.ZERO;
        try {
            if (uid != null) {
                loansOutstanding = loanService.getLoansByUserId(uid).stream()
                    .map(l -> l.getOutstandingAmount() != null ? l.getOutstandingAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
        } catch (Exception ignored) {}

        BigDecimal insuranceCoverTotal = BigDecimal.ZERO;
        try {
            if (uid != null) {
                insuranceCoverTotal = insuranceService.getInsurancePoliciesByUserId(uid).stream()
                    .map(i -> i.getCoverAmount() != null ? i.getCoverAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
        } catch (Exception ignored) {}

        return PortfolioDTOResponse.builder()
                .totalInvestment(valuation.getTotalInvestment())
                .currentValue(valuation.getCurrentValue())
                .totalProfitLoss(valuation.getTotalProfitLoss())
                .totalProfitLossPercentage(valuation.getTotalProfitLossPercentage())
                .sectorAllocation(allocation.getSectorAllocation())
                .marketCapAllocation(allocation.getMarketCapAllocation())
                .score(scoring.getScore())
                .assessment(scoring.getAssessment())
                .recommendations(recommendations)
                .insights(insights)
                .riskSummary(riskSummary)
                .dataFreshness(freshness)
                .build();
    }
}
