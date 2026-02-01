package com.loan.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmortizationScheduleResponse {
    
    private Long loanId;
    private BigDecimal totalPrincipal;
    private BigDecimal totalInterest;
    private BigDecimal totalPayable;
    private Integer tenureMonths;
    private List<AmortizationEntry> schedule;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AmortizationEntry {
        private Integer paymentNumber;
        private LocalDate paymentDate;
        private BigDecimal emiAmount;
        private BigDecimal principalComponent;
        private BigDecimal interestComponent;
        private BigDecimal outstandingBalance;
    }
}
