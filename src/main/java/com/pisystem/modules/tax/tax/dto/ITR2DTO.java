package com.tax.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for ITR-2 JSON generation
 * For individuals/HUFs not having income from business/profession
 * Includes capital gains
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ITR2DTO {
    
    // Extends all fields from ITR1
    private String pan;
    private String name;
    private String dateOfBirth;
    private String aadhaar;
    private String mobileNumber;
    private String emailId;
    
    // Address
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    
    // Income from Salary
    private BigDecimal salaryIncome;
    private BigDecimal allowancesExempt;
    private BigDecimal professionalTax;
    private BigDecimal netSalary;
    
    // Income from House Property  
    private List<HousePropertyDetail> houseProperties;
    
    // Capital Gains
    private BigDecimal shortTermCapitalGain15Percent;
    private BigDecimal shortTermCapitalGainNormal;
    private BigDecimal longTermCapitalGain10Percent;
    private BigDecimal longTermCapitalGain20Percent;
    
    private List<CapitalGainsDetail> capitalGainsDetails;
    
    // Income from Other Sources
    private BigDecimal interestIncome;
    private BigDecimal dividendIncome;
    private BigDecimal otherIncome;
    
    private BigDecimal grossTotalIncome;
    
    // Chapter VI-A Deductions
    private BigDecimal section80C;
    private BigDecimal section80CCD1B;
    private BigDecimal section80D;
    private BigDecimal section80E;
    private BigDecimal section80G;
    private BigDecimal section80TTA;
    private BigDecimal section80TTB;
    private BigDecimal totalChapterVIADeductions;
    
    private BigDecimal totalIncome;
    
    // Tax Computation
    private BigDecimal taxOnTotalIncome;
    private BigDecimal rebate87A;
    private BigDecimal surcharge;
    private BigDecimal healthEducationCess;
    private BigDecimal totalTaxLiability;
    
    // TDS
    private List<TDSDetail> tdsDetails;
    
    // Tax Payments
    private BigDecimal advanceTax;
    private BigDecimal selfAssessmentTax;
    private BigDecimal tdsTotal;
    
    private BigDecimal taxPayable;
    private BigDecimal refundDue;
    
    // Bank Details
    private String bankAccountNumber;
    private String ifscCode;
    private String bankName;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HousePropertyDetail {
        private String address;
        private BigDecimal annualRent;
        private BigDecimal municipalTaxes;
        private BigDecimal interestOnLoan;
        private BigDecimal incomeFromProperty;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapitalGainsDetail {
        private String assetType;
        private String assetDescription;
        private LocalDate purchaseDate;
        private LocalDate saleDate;
        private BigDecimal saleValue;
        private BigDecimal costOfAcquisition;
        private BigDecimal indexedCost;
        private BigDecimal expenses;
        private BigDecimal capitalGain;
        private String gainType; // STCG or LTCG
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TDSDetail {
        private String deductorName;
        private String deductorTAN;
        private BigDecimal incomeChargeable;
        private BigDecimal tdsDeducted;
    }
}
