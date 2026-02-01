package com.tax.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for Business Income computation
 * Supports presumptive taxation (Section 44AD, 44ADA, 44AE)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessIncomeDTO {
    
    @NotNull
    private String businessType; // PROFESSIONAL, TRADING, MANUFACTURING, SERVICE
    
    @NotNull
    private String taxationScheme; // NORMAL, PRESUMPTIVE_44AD, PRESUMPTIVE_44ADA, PRESUMPTIVE_44AE
    
    private String businessName;
    
    private String panNumber;
    
    @PositiveOrZero
    private BigDecimal grossReceipts; // Total turnover/revenue
    
    @PositiveOrZero
    private BigDecimal grossProfit;
    
    // Expenses (for normal taxation)
    @PositiveOrZero
    private BigDecimal salariesAndWages;
    
    @PositiveOrZero
    private BigDecimal rent;
    
    @PositiveOrZero
    private BigDecimal interestOnBorrowedCapital;
    
    @PositiveOrZero
    private BigDecimal depreciation;
    
    @PositiveOrZero
    private BigDecimal otherExpenses;
    
    // Presumptive taxation rates
    private BigDecimal presumptiveRate; // 8% or 6% for 44AD, 50% for 44ADA
    
    // Calculated fields
    private BigDecimal totalExpenses;
    private BigDecimal netProfit;
    private BigDecimal incomeFromBusiness;
    
    // For professionals under 44ADA
    private Boolean isEligibleFor44ADA; // Receipts < 50L
}
