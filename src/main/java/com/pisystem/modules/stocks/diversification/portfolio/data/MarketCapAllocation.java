package com.investments.stocks.diversification.portfolio.data;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MarketCapAllocation {
    private BigDecimal largeCapPercentage;
    private BigDecimal midCapPercentage;
    private BigDecimal smallCapPercentage;
}
