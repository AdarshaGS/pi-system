package com.tax.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for Set-off and Carry Forward of Losses
 * Handles inter-head and intra-head adjustments
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LossSetOffDTO {
    
    private String financialYear;
    
    // Current year losses
    private BigDecimal housePropertyLoss;
    private BigDecimal businessLoss;
    private BigDecimal speculativeBusinessLoss;
    private BigDecimal capitalLossSTCG;
    private BigDecimal capitalLossLTCG;
    
    // Current year incomes (for set-off)
    private BigDecimal salaryIncome;
    private BigDecimal housePropertyIncome;
    private BigDecimal businessIncome;
    private BigDecimal capitalGainSTCG;
    private BigDecimal capitalGainLTCG;
    private BigDecimal otherSourcesIncome;
    
    // After set-off
    private BigDecimal totalIncomeAfterSetOff;
    private BigDecimal lossCarriedForward;
    
    // Previous years' brought forward losses
    private BigDecimal broughtForwardHousePropertyLoss;
    private BigDecimal broughtForwardBusinessLoss;
    private BigDecimal broughtForwardCapitalLoss;
    
    // Set-off summary
    private BigDecimal housePropertyLossSetOff;
    private BigDecimal businessLossSetOff;
    private BigDecimal capitalLossSetOff;
    
    // Remaining losses to carry forward
    private BigDecimal housePropertyLossToCarryForward;
    private BigDecimal businessLossToCarryForward;
    private BigDecimal capitalLossToCarryForward;
    
    // Carry forward limits (years)
    private Integer housePropertyCarryForwardYears; // 8 years
    private Integer businessCarryForwardYears; // 8 years  
    private Integer capitalLossCarryForwardYears; // 8 years
}
