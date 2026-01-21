package com.investments.stocks.diversification.portfolio.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PortfolioAllocationResult {
    private SectorAllocation sectorAllocation;
    private MarketCapAllocation marketCapAllocation;
}
