package com.investments.stocks.diversification.portfolio.data;

import java.math.BigDecimal;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PortfolioAllocationResult {
    private Map<String, BigDecimal> sectorAllocation;
    private MarketCapAllocation marketCapAllocation;
}
