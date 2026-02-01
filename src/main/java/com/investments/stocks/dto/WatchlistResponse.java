package com.investments.stocks.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistResponse {

    private Long userId;
    private Integer totalStocks;
    private List<WatchlistItem> stocks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WatchlistItem {
        private Long id;
        private String symbol;
        private String companyName;
        private BigDecimal currentPrice;
        private BigDecimal change;
        private BigDecimal changePercent;
        private String notes;
        private String addedAt;
    }
}
