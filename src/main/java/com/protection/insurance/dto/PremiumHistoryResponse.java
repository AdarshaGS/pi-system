package com.protection.insurance.dto;

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
public class PremiumHistoryResponse {

    private Long insuranceId;
    private String policyNumber;
    private Integer totalPremiumsPaid;
    private Integer missedPremiums;
    private BigDecimal totalAmountPaid;
    private BigDecimal totalOutstanding;
    private List<PremiumSummary> premiums;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PremiumSummary {
        private Long id;
        private BigDecimal paymentAmount;
        private String paymentDate;
        private String dueDate;
        private String paymentStatus;
        private String paymentMethod;
        private String transactionReference;
        private Boolean isAutoRenewal;
        private String notes;
    }
}
