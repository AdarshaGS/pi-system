package com.investments.stocks.diversification.portfolio.data;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiskAnalysisResult {
    private List<AnalysisInsight> insights;

    // Metrics for scoring
    private double maxStockAllocation;
    private double maxSectorAllocation;
    private double smallCapAllocation;
    private boolean hasSignificantDrawdown; // One or more stocks < -25%
}
