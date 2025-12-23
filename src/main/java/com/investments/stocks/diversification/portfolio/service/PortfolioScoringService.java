package com.investments.stocks.diversification.portfolio.service;

import org.springframework.stereotype.Service;

import com.investments.stocks.diversification.portfolio.data.AnalysisInsight;
import com.investments.stocks.diversification.portfolio.data.PortfolioScoringResult;
import com.investments.stocks.diversification.portfolio.data.RiskAnalysisResult;

@Service
public class PortfolioScoringService {

    public PortfolioScoringResult calculateScore(RiskAnalysisResult riskResult) {
        int score = 100;

        // 1. Single Stock Penalty (Max exposure)
        if (riskResult.getMaxStockAllocation() > 40.0) {
            score -= 35;
        } else if (riskResult.getMaxStockAllocation() > 25.0) {
            score -= 20;
        }

        // 2. Single Sector Penalty
        if (riskResult.getMaxSectorAllocation() > 40.0) {
            score -= 15;
        }

        // 3. Small Cap Exposure
        if (riskResult.getSmallCapAllocation() > 70.0) {
            score -= 15;
        }

        // 4. Stock Drawdown
        if (riskResult.isHasSignificantDrawdown()) {
            score -= 10;
        }

        // 5. Warnings Count
        long warningCount = riskResult.getInsights().stream()
                .filter(i -> i.getType() == AnalysisInsight.InsightType.WARNING)
                .count();

        if (warningCount > 3) {
            score -= 10;
        }

        // Clamp Score
        if (score > 90)
            score = 90;
        if (score < 20)
            score = 20;

        // Assessment
        String assessment = getAssessment(score);

        return PortfolioScoringResult.builder()
                .score(score)
                .assessment(assessment)
                .build();
    }

    private String getAssessment(int score) {
        if (score >= 80)
            return "STRONG_AND_BALANCED";
        if (score >= 65)
            return "MODERATELY_DIVERSIFIED";
        if (score >= 45)
            return "HIGH_RISK";
        return "POORLY_DIVERSIFIED";
    }
}
