package com.loan.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentHistoryResponse {
    
    private Long loanId;
    private Integer totalPayments;
    private Integer missedPayments;
    private BigDecimal totalPaid;
    private BigDecimal totalPrincipalPaid;
    private BigDecimal totalInterestPaid;
    private BigDecimal outstandingBalance;
    private List<PaymentSummary> payments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentSummary {
        private Long paymentId;
        private String paymentDate;
        private BigDecimal amount;
        private BigDecimal principalPaid;
        private BigDecimal interestPaid;
        private String paymentType;
        private String paymentStatus;
        private String paymentMethod;
    }
}
