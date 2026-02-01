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
public class CorporateActionsResponse {

    private String symbol;
    private Integer totalActions;
    private List<ActionItem> actions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionItem {
        private Long id;
        private String actionType;
        private LocalDate announcementDate;
        private LocalDate exDate;
        private LocalDate recordDate;
        private LocalDate paymentDate;
        private BigDecimal dividendAmount;
        private String splitRatio;
        private String bonusRatio;
        private String rightsRatio;
        private BigDecimal rightsPrice;
        private String description;
    }
}
