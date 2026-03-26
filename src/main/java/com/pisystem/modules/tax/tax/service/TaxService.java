package com.tax.service;

import com.tax.data.*;
import java.math.BigDecimal;
import java.util.List;

public interface TaxService {
    
    // Existing methods
    TaxDTO createTaxDetails(Tax tax);
    TaxDTO getTaxDetailsByUserId(Long userId, String financialYear);
    BigDecimal calculateTotalTaxLiability(Long userId);
    BigDecimal getOutstandingTaxLiability(Long userId);
    
    // Tax Regime Comparison
    TaxRegimeComparisonDTO compareTaxRegimes(Long userId, String financialYear, BigDecimal grossIncome);
    
    // Capital Gains Management
    CapitalGainsTransaction recordCapitalGain(CapitalGainsTransaction transaction);
    CapitalGainsSummaryDTO getCapitalGainsSummary(Long userId, String financialYear);
    CapitalGainsTransaction calculateCapitalGains(CapitalGainsTransaction transaction);
    List<CapitalGainsTransaction> getCapitalGainsTransactions(Long userId, String financialYear);
    
    // Tax Saving Recommendations
    TaxSavingRecommendationDTO getTaxSavingRecommendations(Long userId, String financialYear);
    TaxSavingInvestment recordTaxSavingInvestment(TaxSavingInvestment investment);
    List<TaxSavingInvestment> getTaxSavingInvestments(Long userId, String financialYear);
    
    // TDS Tracking
    TDSEntry recordTDSEntry(TDSEntry tdsEntry);
    TDSReconciliationDTO getTDSReconciliation(Long userId, String financialYear);
    List<TDSEntry> getTDSEntries(Long userId, String financialYear);
    TDSEntry updateTDSStatus(Long tdsId, String status);
    
    // Tax Projections
    TaxProjectionDTO getTaxProjection(Long userId, String financialYear);
    
    // ITR Data Export
    ITRPreFillDataDTO getITRPreFillData(Long userId, String financialYear);
}