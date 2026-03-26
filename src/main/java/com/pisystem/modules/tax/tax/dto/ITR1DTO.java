package com.tax.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for ITR-1 (Sahaj) JSON generation
 * For individuals having income from Salaries, one house property, other sources
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ITR1DTO {
    
    // Personal Information
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
    private String country;
    
    // Income Details
    private BigDecimal salaryIncome;
    private BigDecimal allowancesExempt;
    private BigDecimal deductionUS16;
    private BigDecimal netSalary;
    
    private BigDecimal housePropertyIncome;
    
    private BigDecimal interestIncome;
    private BigDecimal otherIncome;
    
    private BigDecimal grossTotalIncome;
    
    // Deductions
    private BigDecimal section80C;
    private BigDecimal section80D;
    private BigDecimal section80G;
    private BigDecimal section80TTA;
    private BigDecimal totalDeductions;
    
    private BigDecimal totalIncome;
    
    // Tax Computation
    private BigDecimal taxOnTotalIncome;
    private BigDecimal rebate87A;
    private BigDecimal surcharge;
    private BigDecimal healthEducationCess;
    private BigDecimal totalTaxLiability;
    
    // TDS
    private List<TDSDetail> tdsDetails;
    
    // Bank Details for Refund
    private String bankAccountNumber;
    private String ifscCode;
    private String bankName;
    
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
