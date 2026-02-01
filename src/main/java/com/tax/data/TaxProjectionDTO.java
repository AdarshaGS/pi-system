package com.tax.data;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxProjectionDTO {
    private Long userId;
    private String financialYear;
    
    // Income Projections
    private BigDecimal projectedSalaryIncome;
    private BigDecimal projectedBusinessIncome;
    private BigDecimal projectedCapitalGains;
    private BigDecimal projectedOtherIncome;
    private BigDecimal projectedGrossIncome;
    
    // Deductions
    private BigDecimal projectedStandardDeduction;
    private BigDecimal projected80CDeductions;
    private BigDecimal projectedOtherDeductions;
    private BigDecimal projectedTotalDeductions;
    
    // Tax Calculation
    private BigDecimal projectedTaxableIncome;
    private BigDecimal projectedTaxLiability;
    private BigDecimal projectedSurcharge;
    private BigDecimal projectedCess;
    private BigDecimal projectedTotalTax;
    
    // Payments
    private BigDecimal tdsPaid;
    private BigDecimal advanceTaxPaid;
    private BigDecimal selfAssessmentTax;
    private BigDecimal totalTaxPaid;
    
    // Balance
    private BigDecimal balanceTaxPayable; // Negative means refund
    
    // Recommendations
    private Map<String, String> monthlyRecommendations;
    private String planningAdvice;
}
