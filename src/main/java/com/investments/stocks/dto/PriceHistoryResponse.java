package com.investments.stocks.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistoryResponse {

    private String symbol;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalRecords;
    private List<PriceData> prices;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceData {
        private LocalDate date;
        private BigDecimal open;
        private BigDecimal high;
        private BigDecimal low;
        private BigDecimal close;
        private Long volume;
    }
}
