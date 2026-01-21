package com.investments.stocks.diversification.portfolio.data;

import java.math.BigDecimal;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SectorAllocation {
    private Map<String, BigDecimal> sectorAllocation;
}
