package com.loan.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanAnalysisResponse {
    
    private Long loanId;
    private BigDecimal totalInterestPayable;
    private BigDecimal totalAmountPayable;
    private BigDecimal interestToPrincipalRatio;
    private BigDecimal effectiveInterestRate;
    private Integer remainingTenureMonths;
    private BigDecimal remainingInterest;
    private Integer paymentsCompleted;
    private Integer totalPayments;
    private BigDecimal completionPercentage;
}
