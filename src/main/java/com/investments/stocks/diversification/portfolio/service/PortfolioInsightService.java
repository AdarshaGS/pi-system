package com.investments.stocks.diversification.portfolio.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.investments.stocks.diversification.portfolio.data.AnalysisInsight;
import com.investments.stocks.diversification.portfolio.data.PortfolioInsightsDTO;
import com.investments.stocks.diversification.portfolio.data.RiskSummary;

@Service
public class PortfolioInsightService {

    public PortfolioInsightsDTO groupInsights(List<AnalysisInsight> insights) {
        Map<AnalysisInsight.InsightType, List<AnalysisInsight>> grouped = insights.stream()
                .collect(Collectors.groupingBy(AnalysisInsight::getType));

        return PortfolioInsightsDTO.builder()
                .critical(grouped.getOrDefault(AnalysisInsight.InsightType.CRITICAL, new ArrayList<>()))
                .warning(grouped.getOrDefault(AnalysisInsight.InsightType.WARNING, new ArrayList<>()))
                .info(grouped.getOrDefault(AnalysisInsight.InsightType.INFO, new ArrayList<>()))
                .build();
    }

    public RiskSummary calculateRiskSummary(List<AnalysisInsight> insights) {
        int critical = 0;
        int warning = 0;
        int info = 0;

        for (AnalysisInsight insight : insights) {
            switch (insight.getType()) {
                case CRITICAL:
                    critical++;
                    break;
                case WARNING:
                    warning++;
                    break;
                case INFO:
                case OPPORTUNITY: // Treating Opportunity as Info? Or ignore? Prompt says "info".
                    info++;
                    break;
                default:
                    break;
            }
        }

        return RiskSummary.builder()
                .criticalCount(critical)
                .warningCount(warning)
                .infoCount(info)
                .build();
    }
}
