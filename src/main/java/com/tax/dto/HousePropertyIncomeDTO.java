package com.tax.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for House Property Income calculation
 * Supports income from let-out, self-occupied, and deemed let-out properties
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HousePropertyIncomeDTO {
    
    @NotNull
    private String propertyType; // SELF_OCCUPIED, LET_OUT, DEEMED_LET_OUT
    
    private String address;
    
    @PositiveOrZero
    private BigDecimal annualRent; // Actual rent received or receivable
    
    @PositiveOrZero
    private BigDecimal municipalTaxes; // Property tax paid to local authority
    
    @PositiveOrZero
    private BigDecimal interestOnHomeLoan; // Interest paid on housing loan
    
    @PositiveOrZero
    private BigDecimal principalRepayment; // For 80C deduction
    
    private Boolean isCoOwned;
    
    private Integer ownershipPercentage; // Co-ownership percentage
    
    // Calculated fields
    private BigDecimal grossAnnualValue;
    private BigDecimal netAnnualValue;
    private BigDecimal standardDeduction; // 30% of NAV
    private BigDecimal incomeFromHouseProperty;
    
    // Pre-construction interest
    private BigDecimal preConstructionInterest; // 1/5th deductible per year
    
    // For self-occupied
    private Integer numberOfSelfOccupied; // Max 2 self-occupied properties
}
