package com.protection.insurance.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoverageAnalysisResponse {

    private Long userId;
    
    // Life Insurance Analysis
    private LifeInsuranceAnalysis lifeInsurance;
    
    // Health Insurance Analysis
    private HealthInsuranceAnalysis healthInsurance;
    
    // Overall Summary
    private BigDecimal totalCoverAmount;
    private BigDecimal totalAnnualPremium;
    private String overallCoverageStatus; // ADEQUATE, UNDER_INSURED, OVER_INSURED
    private String recommendations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LifeInsuranceAnalysis {
        private BigDecimal currentCoverage;
        private BigDecimal recommendedCoverage;
        private BigDecimal coverageGap;
        private String adequacyStatus; // ADEQUATE, INADEQUATE, OVER_INSURED
        private String recommendation;
        
        // Calculation basis
        private BigDecimal annualIncome;
        private Integer multiplier; // Typically 10-15x annual income
        private BigDecimal outstandingLiabilities;
        private Integer dependentsCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthInsuranceAnalysis {
        private BigDecimal currentCoverage;
        private BigDecimal recommendedCoverage;
        private BigDecimal coverageGap;
        private String adequacyStatus; // ADEQUATE, INADEQUATE, OVER_INSURED
        private String recommendation;
        
        // Calculation basis
        private Integer familySize;
        private String city; // Metro/Non-Metro affects costs
        private BigDecimal averageHospitalizationCost;
    }
}
