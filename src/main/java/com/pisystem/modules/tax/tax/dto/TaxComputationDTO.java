package com.tax.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for Complete Tax Computation
 * Includes all heads of income, deductions, rebates, and cess
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxComputationDTO {
    
    private String financialYear;
    private String assessmentYear;
    
    // Income from all heads
    private BigDecimal salaryIncome;
    private BigDecimal housePropertyIncome;
    private BigDecimal businessIncome;
    private BigDecimal capitalGainsSTCG;
    private BigDecimal capitalGainsLTCG;
    private BigDecimal otherSourcesIncome;
    
    // Gross Total Income
    private BigDecimal grossTotalIncome;
    
    // Chapter VI-A Deductions
    private BigDecimal deduction80C;      // ₹1.5L
    private BigDecimal deduction80CCD1B;  // ₹50K (NPS)
    private BigDecimal deduction80D;      // ₹25K/₹50K
    private BigDecimal deduction80E;      // Education loan interest
    private BigDecimal deduction80G;      // Donations
    private BigDecimal deduction80TTA;    // ₹10K savings interest
    private BigDecimal deduction80TTB;    // ₹50K for senior citizens
    private BigDecimal totalChapterVIADeductions;
    
    // Total Income
    private BigDecimal totalIncome;
    
    // Tax on Total Income
    private BigDecimal taxOnTotalIncome;
    
    // Rebate under Section 87A
    private BigDecimal rebate87A;        // ₹12,500 if income ≤ ₹5L (old) / ₹7L (new)
    
    // Tax after rebate
    private BigDecimal taxAfterRebate;
    
    // Surcharge
    private BigDecimal surcharge;        // 10% (₹50L-1Cr), 15% (₹1-2Cr), 25% (₹2-5Cr), 37% (>₹5Cr)
    private BigDecimal taxAfterSurcharge;
    
    // Health and Education Cess (4%)
    private BigDecimal healthEducationCess;
    
    // Total Tax Liability
    private BigDecimal totalTaxLiability;
    
    // TDS Already Paid
    private BigDecimal tdsAlreadyPaid;
    
    // Advance Tax/Self Assessment Tax
    private BigDecimal advanceTaxPaid;
    private BigDecimal selfAssessmentTaxPaid;
    
    // Tax Payable or Refundable
    private BigDecimal taxPayable;
    private BigDecimal taxRefundable;
    
    // Regime used
    private String regimeUsed; // OLD_REGIME or NEW_REGIME
}
