package com.pisystem.modules.stocks.diversification.portfolio.data;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PortfolioValuationResult {
    private BigDecimal totalInvestment;
    private BigDecimal currentValue;
    private BigDecimal totalProfitLoss;
    private BigDecimal totalProfitLossPercentage;
    private BigDecimal realizedGain;
    private BigDecimal unrealizedGain;
    private BigDecimal xirr;
}
