package com.tax.service;

import com.tax.data.CapitalGainsTransaction;
import com.tax.data.TaxSavingInvestment;

import java.util.List;

/**
 * Service for auto-populating tax data from other modules
 * Links portfolio, FD, insurance, and income data to tax records
 */
public interface TaxAutoPopulationService {
    
    /**
     * Auto-calculate capital gains from portfolio transactions
     * Fetches buy/sell transactions and computes STCG/LTCG
     */
    List<CapitalGainsTransaction> autoPopulateCapitalGains(Long userId, String financialYear);
    
    /**
     * Auto-populate salary income from income/payroll module
     * Fetches salary slips and computes gross salary
     */
    void autoPopulateSalaryIncome(Long userId, String financialYear);
    
    /**
     * Auto-fetch interest income from FD and savings accounts
     * Aggregates interest earned across all accounts
     */
    void autoPopulateInterestIncome(Long userId, String financialYear);
    
    /**
     * Auto-detect dividend income from stock holdings
     * Fetches dividend announcements and calculates dividend income
     */
    void autoPopulateDividendIncome(Long userId, String financialYear);
    
    /**
     * Auto-populate 80C investments from FD, insurance, PPF, ELSS
     * Links to actual investment records
     */
    List<TaxSavingInvestment> autoPopulate80CInvestments(Long userId, String financialYear);
    
    /**
     * Auto-populate 80D investments from health insurance premiums
     */
    List<TaxSavingInvestment> autoPopulate80DInvestments(Long userId, String financialYear);
    
    /**
     * Auto-populate home loan interest for 24B and 80EEA
     */
    void autoPopulateHomeLoanInterest(Long userId, String financialYear);
}
