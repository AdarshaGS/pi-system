package com.tax.service;

import com.tax.dto.*;

import java.math.BigDecimal;

/**
 * Service for advanced tax calculations
 * Handles house property income, business income, surcharge, cess, and rebate
 */
public interface TaxCalculationService {
    
    /**
     * Calculate income from house property
     * Supports self-occupied, let-out, and deemed let-out properties
     */
    HousePropertyIncomeDTO calculateHousePropertyIncome(HousePropertyIncomeDTO input);
    
    /**
     * Calculate business income
     * Supports normal taxation and presumptive taxation (44AD, 44ADA, 44AE)
     */
    BusinessIncomeDTO calculateBusinessIncome(BusinessIncomeDTO input);
    
    /**
     * Perform set-off and carry forward of losses
     * Handles inter-head and intra-head adjustments
     */
    LossSetOffDTO processLossSetOff(LossSetOffDTO input);
    
    /**
     * Calculate complete tax liability with surcharge and cess
     * Includes rebate under Section 87A
     */
    TaxComputationDTO calculateCompleteTax(TaxComputationDTO input);
    
    /**
     * Calculate rebate under Section 87A
     * ₹12,500 if income ≤ ₹5L (old regime) or ≤ ₹7L (new regime)
     */
    BigDecimal calculateRebate87A(BigDecimal totalIncome, String regime);
    
    /**
     * Calculate surcharge based on income slabs
     * 10% (₹50L-1Cr), 15% (₹1-2Cr), 25% (₹2-5Cr), 37% (>₹5Cr)
     */
    BigDecimal calculateSurcharge(BigDecimal taxAmount, BigDecimal totalIncome);
    
    /**
     * Calculate Health and Education Cess (4% of tax + surcharge)
     */
    BigDecimal calculateHealthEducationCess(BigDecimal taxAfterSurcharge);
}
