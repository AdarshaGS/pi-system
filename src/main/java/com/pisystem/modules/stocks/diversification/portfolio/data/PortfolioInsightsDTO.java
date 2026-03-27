package com.pisystem.modules.stocks.diversification.portfolio.data;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PortfolioInsightsDTO {
    private List<AnalysisInsight> critical;
    private List<AnalysisInsight> warning;
    private List<AnalysisInsight> info;
}
