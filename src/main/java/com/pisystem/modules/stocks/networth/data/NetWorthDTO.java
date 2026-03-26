package com.investments.stocks.networth.data;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

import com.common.data.EntityType;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class NetWorthDTO {
    private BigDecimal totalAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal netWorth;
    private BigDecimal portfolioValue;
    private BigDecimal savingsValue;
    private BigDecimal outstandingLoans;
    private BigDecimal outstandingTaxLiability;
    private BigDecimal outstandingLendings;
    private BigDecimal netWorthAfterTax;
    private Map<EntityType, BigDecimal> assetBreakdown;
    private Map<EntityType, BigDecimal> liabilityBreakdown;
}
