package com.investments.stocks.diversification.portfolio.data;

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
}
