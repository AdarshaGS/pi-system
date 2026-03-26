package com.tax.data;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ITRPreFillDataDTO {
    private Long userId;
    private String financialYear;
    private String assessmentYear;
    
    // Personal Information
    private String pan;
    private String name;
    private String dateOfBirth;
    private String address;
    
    // Salary Income (Form 16)
    private BigDecimal grossSalary;
    private BigDecimal standardDeduction;
    private BigDecimal professionalTax;
    private BigDecimal netSalary;
    
    // House Property Income
    private BigDecimal rentalIncome;
    private BigDecimal housingLoanInterest;
    private BigDecimal netHousePropertyIncome;
    
    // Capital Gains
    private BigDecimal shortTermCapitalGains;
    private BigDecimal longTermCapitalGains;
    private List<CapitalGainsDetail> capitalGainsSchedule;
    
    // Other Income
    private BigDecimal interestIncome;
    private BigDecimal dividendIncome;
    private BigDecimal otherIncome;
    
    // Deductions Chapter VI-A
    private Map<String, BigDecimal> deductions80C;
    private BigDecimal total80CDeductions;
    private Map<String, BigDecimal> otherDeductions;
    
    // TDS Details
    private List<TDSDetail> tdsDetails;
    private BigDecimal totalTDS;
    
    // Tax Computation
    private BigDecimal grossTotalIncome;
    private BigDecimal totalDeductions;
    private BigDecimal taxableIncome;
    private BigDecimal taxOnTaxableIncome;
    private BigDecimal surcharge;
    private BigDecimal healthEducationCess;
    private BigDecimal totalTaxLiability;
    
    // Tax Payments
    private BigDecimal advanceTax;
    private BigDecimal selfAssessmentTax;
    private BigDecimal totalTaxPaid;
    
    // Balance
    private BigDecimal refundOrDemand;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CapitalGainsDetail {
        private String assetDescription;
        private String dateOfPurchase;
        private String dateOfSale;
        private BigDecimal costOfAcquisition;
        private BigDecimal saleConsideration;
        private BigDecimal capitalGain;
        private String gainType;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TDSDetail {
        private String deductorName;
        private String deductorTAN;
        private String section;
        private BigDecimal incomeAmount;
        private BigDecimal tdsAmount;
        private String certificateNumber;
    }
}
