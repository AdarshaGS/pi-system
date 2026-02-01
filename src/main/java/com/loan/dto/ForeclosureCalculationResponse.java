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
public class ForeclosureCalculationResponse {
    
    private Long loanId;
    private BigDecimal outstandingPrincipal;
    private BigDecimal outstandingInterest;
    private BigDecimal foreclosureCharges;
    private BigDecimal foreclosureChargesPercentage;
    private BigDecimal totalForeclosureAmount;
    private String message;
}
