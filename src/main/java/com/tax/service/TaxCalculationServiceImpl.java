package com.tax.service;

import com.tax.dto.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Implementation of advanced tax calculations
 */
@Service
public class TaxCalculationServiceImpl implements TaxCalculationService {

    // Tax slabs for FY 2023-24 (Old Regime)
    private static final BigDecimal SLAB_0 = new BigDecimal("250000");
    private static final BigDecimal SLAB_1 = new BigDecimal("500000");
    private static final BigDecimal SLAB_2 = new BigDecimal("1000000");
    
    // Surcharge slabs
    private static final BigDecimal SURCHARGE_SLAB_1 = new BigDecimal("5000000");
    private static final BigDecimal SURCHARGE_SLAB_2 = new BigDecimal("10000000");
    private static final BigDecimal SURCHARGE_SLAB_3 = new BigDecimal("20000000");
    private static final BigDecimal SURCHARGE_SLAB_4 = new BigDecimal("50000000");
    
    // Rebate and cess rates
    private static final BigDecimal REBATE_87A_OLD_REGIME = new BigDecimal("500000");
    private static final BigDecimal REBATE_87A_NEW_REGIME = new BigDecimal("700000");
    private static final BigDecimal REBATE_87A_AMOUNT = new BigDecimal("12500");
    private static final BigDecimal CESS_RATE = new BigDecimal("0.04");
    
    // House property constants
    private static final BigDecimal STANDARD_DEDUCTION_RATE = new BigDecimal("0.30");
    private static final BigDecimal ANNUAL_VALUE_UNREALIZED_RENT = new BigDecimal("0.12");
    
    // Presumptive taxation rates
    private static final BigDecimal RATE_44AD_CASH = new BigDecimal("0.08");
    private static final BigDecimal RATE_44AD_DIGITAL = new BigDecimal("0.06");
    private static final BigDecimal RATE_44ADA = new BigDecimal("0.50");

    @Override
    public HousePropertyIncomeDTO calculateHousePropertyIncome(HousePropertyIncomeDTO input) {
        BigDecimal grossAnnualValue;
        
        if ("SELF_OCCUPIED".equals(input.getPropertyType())) {
            grossAnnualValue = BigDecimal.ZERO; // Self-occupied is nil
        } else { // LET_OUT or DEEMED_LET_OUT
            grossAnnualValue = input.getAnnualRent() != null ? input.getAnnualRent() : BigDecimal.ZERO;
        }
        
        input.setGrossAnnualValue(grossAnnualValue);
        
        // Net Annual Value = GAV - Municipal taxes
        BigDecimal municipalTaxes = input.getMunicipalTaxes() != null ? input.getMunicipalTaxes() : BigDecimal.ZERO;
        BigDecimal netAnnualValue = grossAnnualValue.subtract(municipalTaxes);
        input.setNetAnnualValue(netAnnualValue);
        
        // Standard deduction = 30% of NAV
        BigDecimal standardDeduction = netAnnualValue.multiply(STANDARD_DEDUCTION_RATE).setScale(0, RoundingMode.HALF_UP);
        input.setStandardDeduction(standardDeduction);
        
        // Interest on home loan deduction
        BigDecimal interestDeduction = input.getInterestOnHomeLoan() != null ? input.getInterestOnHomeLoan() : BigDecimal.ZERO;
        
        // Income from house property = NAV - Standard Deduction - Interest
        BigDecimal incomeFromHP = netAnnualValue.subtract(standardDeduction).subtract(interestDeduction);
        input.setIncomeFromHouseProperty(incomeFromHP);
        
        return input;
    }

    @Override
    public BusinessIncomeDTO calculateBusinessIncome(BusinessIncomeDTO input) {
        BigDecimal incomeFromBusiness;
        
        if ("PRESUMPTIVE_44AD".equals(input.getTaxationScheme())) {
            // Section 44AD: 8% for cash, 6% for digital payments
            // Simplified: Use 8% for all receipts
            BigDecimal grossReceipts = input.getGrossReceipts() != null ? input.getGrossReceipts() : BigDecimal.ZERO;
            incomeFromBusiness = grossReceipts.multiply(RATE_44AD_CASH);
            
        } else if ("PRESUMPTIVE_44ADA".equals(input.getTaxationScheme())) {
            // Section 44ADA: 50% for professionals (receipts < ₹50L)
            BigDecimal grossReceipts = input.getGrossReceipts() != null ? input.getGrossReceipts() : BigDecimal.ZERO;
            incomeFromBusiness = grossReceipts.multiply(RATE_44ADA);
            
        } else if ("PRESUMPTIVE_44AE".equals(input.getTaxationScheme())) {
            // Section 44AE: ₹7,500 per vehicle per month (goods carriage)
            // Simplified calculation
            BigDecimal grossReceipts = input.getGrossReceipts() != null ? input.getGrossReceipts() : BigDecimal.ZERO;
            incomeFromBusiness = grossReceipts;
            
        } else { // NORMAL
            // Normal taxation: Gross Receipts - Expenses
            BigDecimal grossReceipts = input.getGrossReceipts() != null ? input.getGrossReceipts() : BigDecimal.ZERO;
            BigDecimal totalExpenses = calculateTotalExpenses(input);
            input.setTotalExpenses(totalExpenses);
            incomeFromBusiness = grossReceipts.subtract(totalExpenses);
        }
        
        input.setIncomeFromBusiness(incomeFromBusiness);
        return input;
    }
    
    private BigDecimal calculateTotalExpenses(BusinessIncomeDTO input) {
        BigDecimal total = BigDecimal.ZERO;
        
        if (input.getSalariesAndWages() != null) total = total.add(input.getSalariesAndWages());
        if (input.getRent() != null) total = total.add(input.getRent());
        if (input.getDepreciation() != null) total = total.add(input.getDepreciation());
        if (input.getInterestOnBorrowedCapital() != null) total = total.add(input.getInterestOnBorrowedCapital());
        if (input.getOtherExpenses() != null) total = total.add(input.getOtherExpenses());
        
        return total;
    }

    @Override
    public LossSetOffDTO processLossSetOff(LossSetOffDTO input) {
        // Rule: House property loss can be set off against any head (max ₹2L per year)
        // Rule: Business loss can be set off against any head except salary
        // Rule: Speculative business loss can only be set off against speculative income
        // Rule: STCG/LTCG loss can be set off against capital gains only
        
        BigDecimal hpLoss = input.getHousePropertyLoss() != null ? input.getHousePropertyLoss() : BigDecimal.ZERO;
        BigDecimal businessLoss = input.getBusinessLoss() != null ? input.getBusinessLoss() : BigDecimal.ZERO;
        BigDecimal stcgLoss = input.getCapitalLossSTCG() != null ? input.getCapitalLossSTCG() : BigDecimal.ZERO;
        BigDecimal ltcgLoss = input.getCapitalLossLTCG() != null ? input.getCapitalLossLTCG() : BigDecimal.ZERO;
        
        // House property loss set-off (max ₹2L)
        BigDecimal maxHPSetOff = new BigDecimal("200000");
        BigDecimal hpSetOffAmount = hpLoss.min(maxHPSetOff);
        
        // Set off HP loss against other incomes
        BigDecimal totalOtherIncome = BigDecimal.ZERO;
        if (input.getSalaryIncome() != null) totalOtherIncome = totalOtherIncome.add(input.getSalaryIncome());
        if (input.getBusinessIncome() != null) totalOtherIncome = totalOtherIncome.add(input.getBusinessIncome());
        if (input.getOtherSourcesIncome() != null) totalOtherIncome = totalOtherIncome.add(input.getOtherSourcesIncome());
        
        BigDecimal hpSetOffUsed = hpSetOffAmount.min(totalOtherIncome);
        BigDecimal hpLossCarriedForward = hpLoss.subtract(hpSetOffUsed);
        
        // Capital gains loss set-off (STCG can be set off against STCG and LTCG, LTCG only against LTCG)
        BigDecimal cgIncome = BigDecimal.ZERO;
        if (input.getCapitalGainSTCG() != null) cgIncome = cgIncome.add(input.getCapitalGainSTCG());
        if (input.getCapitalGainLTCG() != null) cgIncome = cgIncome.add(input.getCapitalGainLTCG());
        
        BigDecimal stcgSetOffUsed = stcgLoss.min(cgIncome);
        BigDecimal remainingCGIncome = cgIncome.subtract(stcgSetOffUsed);
        BigDecimal ltcgSetOffUsed = ltcgLoss.min(remainingCGIncome);
        
        BigDecimal stcgLossCarriedForward = stcgLoss.subtract(stcgSetOffUsed);
        BigDecimal ltcgLossCarriedForward = ltcgLoss.subtract(ltcgSetOffUsed);
        
        // Business loss set-off (cannot be set off against salary)
        BigDecimal nonSalaryIncome = totalOtherIncome.subtract(input.getSalaryIncome() != null ? input.getSalaryIncome() : BigDecimal.ZERO);
        BigDecimal businessSetOffUsed = businessLoss.min(nonSalaryIncome);
        BigDecimal businessLossCarriedForward = businessLoss.subtract(businessSetOffUsed);
        
        // Set carry forward amounts
        input.setHousePropertyLossToCarryForward(hpLossCarriedForward);
        input.setBusinessLossToCarryForward(businessLossCarriedForward);
        input.setCapitalLossToCarryForward(stcgLossCarriedForward.add(ltcgLossCarriedForward));
        
        // Set carry forward years (8 years for all)
        input.setHousePropertyCarryForwardYears(8);
        input.setBusinessCarryForwardYears(8);
        input.setCapitalLossCarryForwardYears(8);
        
        return input;
    }

    @Override
    public TaxComputationDTO calculateCompleteTax(TaxComputationDTO input) {
        // Calculate Gross Total Income
        BigDecimal gti = BigDecimal.ZERO;
        if (input.getSalaryIncome() != null) gti = gti.add(input.getSalaryIncome());
        if (input.getHousePropertyIncome() != null) gti = gti.add(input.getHousePropertyIncome());
        if (input.getBusinessIncome() != null) gti = gti.add(input.getBusinessIncome());
        if (input.getCapitalGainsSTCG() != null) gti = gti.add(input.getCapitalGainsSTCG());
        if (input.getCapitalGainsLTCG() != null) gti = gti.add(input.getCapitalGainsLTCG());
        if (input.getOtherSourcesIncome() != null) gti = gti.add(input.getOtherSourcesIncome());
        input.setGrossTotalIncome(gti);
        
        // Calculate Total Deductions (Chapter VI-A)
        BigDecimal totalDeductions = BigDecimal.ZERO;
        if (input.getDeduction80C() != null) totalDeductions = totalDeductions.add(input.getDeduction80C());
        if (input.getDeduction80CCD1B() != null) totalDeductions = totalDeductions.add(input.getDeduction80CCD1B());
        if (input.getDeduction80D() != null) totalDeductions = totalDeductions.add(input.getDeduction80D());
        if (input.getDeduction80E() != null) totalDeductions = totalDeductions.add(input.getDeduction80E());
        if (input.getDeduction80G() != null) totalDeductions = totalDeductions.add(input.getDeduction80G());
        if (input.getDeduction80TTA() != null) totalDeductions = totalDeductions.add(input.getDeduction80TTA());
        if (input.getDeduction80TTB() != null) totalDeductions = totalDeductions.add(input.getDeduction80TTB());
        input.setTotalChapterVIADeductions(totalDeductions);
        
        // Total Income = GTI - Deductions
        BigDecimal totalIncome = gti.subtract(totalDeductions);
        if (totalIncome.compareTo(BigDecimal.ZERO) < 0) totalIncome = BigDecimal.ZERO;
        input.setTotalIncome(totalIncome);
        
        // Calculate tax on total income (old regime slabs)
        BigDecimal taxOnIncome = calculateTaxOnIncome(totalIncome);
        input.setTaxOnTotalIncome(taxOnIncome);
        
        // Calculate Rebate under 87A
        BigDecimal rebate = calculateRebate87A(totalIncome, input.getRegimeUsed() != null ? input.getRegimeUsed() : "OLD");
        input.setRebate87A(rebate);
        
        // Tax after rebate
        BigDecimal taxAfterRebate = taxOnIncome.subtract(rebate);
        if (taxAfterRebate.compareTo(BigDecimal.ZERO) < 0) taxAfterRebate = BigDecimal.ZERO;
        
        // Calculate Surcharge
        BigDecimal surcharge = calculateSurcharge(taxAfterRebate, totalIncome);
        input.setSurcharge(surcharge);
        
        // Tax after surcharge
        BigDecimal taxAfterSurcharge = taxAfterRebate.add(surcharge);
        
        // Calculate Health and Education Cess
        BigDecimal cess = calculateHealthEducationCess(taxAfterSurcharge);
        input.setHealthEducationCess(cess);
        
        // Total Tax Liability
        BigDecimal totalTaxLiability = taxAfterSurcharge.add(cess);
        input.setTotalTaxLiability(totalTaxLiability);
        
        // Calculate tax payable or refundable
        BigDecimal totalPayments = BigDecimal.ZERO;
        if (input.getTdsAlreadyPaid() != null) totalPayments = totalPayments.add(input.getTdsAlreadyPaid());
        if (input.getAdvanceTaxPaid() != null) totalPayments = totalPayments.add(input.getAdvanceTaxPaid());
        if (input.getSelfAssessmentTaxPaid() != null) totalPayments = totalPayments.add(input.getSelfAssessmentTaxPaid());
        
        BigDecimal difference = totalTaxLiability.subtract(totalPayments);
        if (difference.compareTo(BigDecimal.ZERO) > 0) {
            input.setTaxPayable(difference);
            input.setTaxRefundable(BigDecimal.ZERO);
        } else {
            input.setTaxPayable(BigDecimal.ZERO);
            input.setTaxRefundable(difference.abs());
        }
        
        return input;
    }
    
    private BigDecimal calculateTaxOnIncome(BigDecimal income) {
        BigDecimal tax = BigDecimal.ZERO;
        
        if (income.compareTo(SLAB_0) <= 0) {
            return BigDecimal.ZERO; // No tax up to ₹2.5L
        }
        
        if (income.compareTo(SLAB_1) <= 0) {
            // 5% on income between ₹2.5L and ₹5L
            tax = income.subtract(SLAB_0).multiply(new BigDecimal("0.05"));
        } else if (income.compareTo(SLAB_2) <= 0) {
            // 5% on ₹2.5L and 20% on income between ₹5L and ₹10L
            tax = SLAB_1.subtract(SLAB_0).multiply(new BigDecimal("0.05"))
                .add(income.subtract(SLAB_1).multiply(new BigDecimal("0.20")));
        } else {
            // 5% on first ₹2.5L, 20% on next ₹5L, 30% on income above ₹10L
            tax = SLAB_1.subtract(SLAB_0).multiply(new BigDecimal("0.05"))
                .add(SLAB_2.subtract(SLAB_1).multiply(new BigDecimal("0.20")))
                .add(income.subtract(SLAB_2).multiply(new BigDecimal("0.30")));
        }
        
        return tax.setScale(0, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateRebate87A(BigDecimal totalIncome, String regime) {
        BigDecimal threshold = "NEW".equalsIgnoreCase(regime) ? REBATE_87A_NEW_REGIME : REBATE_87A_OLD_REGIME;
        
        if (totalIncome.compareTo(threshold) <= 0) {
            return REBATE_87A_AMOUNT;
        }
        
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateSurcharge(BigDecimal taxAmount, BigDecimal totalIncome) {
        BigDecimal surchargeRate;
        
        if (totalIncome.compareTo(SURCHARGE_SLAB_4) > 0) {
            surchargeRate = new BigDecimal("0.37"); // 37% for income > ₹5Cr
        } else if (totalIncome.compareTo(SURCHARGE_SLAB_3) > 0) {
            surchargeRate = new BigDecimal("0.25"); // 25% for ₹2-5Cr
        } else if (totalIncome.compareTo(SURCHARGE_SLAB_2) > 0) {
            surchargeRate = new BigDecimal("0.15"); // 15% for ₹1-2Cr
        } else if (totalIncome.compareTo(SURCHARGE_SLAB_1) > 0) {
            surchargeRate = new BigDecimal("0.10"); // 10% for ₹50L-1Cr
        } else {
            return BigDecimal.ZERO; // No surcharge below ₹50L
        }
        
        return taxAmount.multiply(surchargeRate).setScale(0, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateHealthEducationCess(BigDecimal taxAfterSurcharge) {
        return taxAfterSurcharge.multiply(CESS_RATE).setScale(0, RoundingMode.HALF_UP);
    }
}
