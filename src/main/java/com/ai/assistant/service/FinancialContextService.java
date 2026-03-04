package com.ai.assistant.service;

import com.investments.stocks.networth.data.NetWorthDTO;
import com.investments.stocks.networth.service.NetWorthReadService;
import com.budget.data.BudgetReportDTO;
import com.budget.service.BudgetService;
import com.loan.data.Loan;
import com.loan.service.LoanService;
import com.protection.insurance.dto.CoverageAnalysisResponse;
import com.protection.insurance.service.InsuranceService;
import com.users.data.UserProfileResponse;
import com.users.service.UserProfileService;
import com.budget.service.BudgetRecurringTransactionService;
import com.investments.stocks.service.InvestmentRecurringTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FinancialContextService {

    private final NetWorthReadService netWorthService;
    private final BudgetService budgetService;
    private final LoanService loanService;
    private final InsuranceService insuranceService;
    private final UserProfileService userProfileService;
    private final BudgetRecurringTransactionService budgetRecurringService;
    private final InvestmentRecurringTransactionService investmentRecurringService;

    public Map<String, Object> getUserFinancialSnapshot(Long userId) {
        Map<String, Object> snapshot = new HashMap<>();

        try {
            // 1. Profile Info
            UserProfileResponse profile = userProfileService.getProfile(userId);
            Map<String, Object> profileMap = new HashMap<>();
            if (profile.getAge() != null)
                profileMap.put("age", profile.getAge());
            if (profile.getAnnualIncome() != null)
                profileMap.put("annualIncome", profile.getAnnualIncome());
            if (profile.getEmploymentType() != null)
                profileMap.put("employmentType", profile.getEmploymentType());
            if (profile.getDependents() != null)
                profileMap.put("dependents", profile.getDependents());
            if (profile.getRiskTolerance() != null)
                profileMap.put("riskTolerance", profile.getRiskTolerance());
            snapshot.put("profile", profileMap);

            // 2. Net Worth Summary (Enhanced)
            NetWorthDTO netWorth = netWorthService.getNetWorth(userId);
            Map<String, Object> nwMap = new HashMap<>();
            if (netWorth.getTotalAssets() != null)
                nwMap.put("totalAssets", netWorth.getTotalAssets());
            if (netWorth.getTotalLiabilities() != null)
                nwMap.put("totalLiabilities", netWorth.getTotalLiabilities());
            if (netWorth.getNetWorth() != null)
                nwMap.put("netWorthPreTax", netWorth.getNetWorth());
            if (netWorth.getNetWorthAfterTax() != null)
                nwMap.put("netWorthPostTax", netWorth.getNetWorthAfterTax());
            if (netWorth.getPortfolioValue() != null)
                nwMap.put("portfolioValue", netWorth.getPortfolioValue());
            if (netWorth.getSavingsValue() != null)
                nwMap.put("savingsValue", netWorth.getSavingsValue());
            if (netWorth.getOutstandingLoans() != null)
                nwMap.put("outstandingLoans", netWorth.getOutstandingLoans());
            if (netWorth.getOutstandingTaxLiability() != null)
                nwMap.put("outstandingTax", netWorth.getOutstandingTaxLiability());
            if (netWorth.getOutstandingLendings() != null)
                nwMap.put("outstandingLendings", netWorth.getOutstandingLendings());
            if (netWorth.getAssetBreakdown() != null)
                nwMap.put("assetBreakdown", netWorth.getAssetBreakdown());
            if (netWorth.getLiabilityBreakdown() != null)
                nwMap.put("liabilityBreakdown", netWorth.getLiabilityBreakdown());

            snapshot.put("netWorth", nwMap);

            // 3. Budget & Expenses
            try {
                BudgetReportDTO budget = budgetService.getMonthlyReport(userId, null);
                Map<String, Object> budgetMap = new HashMap<>();
                if (budget.getTotalBudget() != null)
                    budgetMap.put("monthlyBudget", budget.getTotalBudget());
                if (budget.getTotalSpent() != null)
                    budgetMap.put("totalSpent", budget.getTotalSpent());
                if (budget.getRemainingBudget() != null)
                    budgetMap.put("remaining", budget.getRemainingBudget());
                budgetMap.put("topCategories",
                        budget.getCategoryBreakdown() != null ? budget.getCategoryBreakdown() : Map.of());
                snapshot.put("budget", budgetMap);
            } catch (Exception e) {
                snapshot.put("budget", "No active budget found");
            }

            // 4. Loans & Liabilities
            List<Loan> loans = loanService.getLoansByUserId(userId);
            snapshot.put("loans", loans.stream().map(l -> {
                Map<String, Object> lMap = new HashMap<>();
                if (l.getLoanType() != null)
                    lMap.put("type", l.getLoanType());
                if (l.getOutstandingAmount() != null)
                    lMap.put("outstanding", l.getOutstandingAmount());
                if (l.getEmiAmount() != null)
                    lMap.put("emi", l.getEmiAmount());
                if (l.getInterestRate() != null)
                    lMap.put("interestRate", l.getInterestRate());
                return lMap;
            }).toList());

            // 5. Insurance Coverage
            CoverageAnalysisResponse insurance = insuranceService.analyzeCoverage(userId);
            Map<String, Object> insMap = new HashMap<>();
            if (insurance.getOverallCoverageStatus() != null)
                insMap.put("overallStatus", insurance.getOverallCoverageStatus());
            if (insurance.getLifeInsurance() != null)
                insMap.put("lifeCoverageGap", insurance.getLifeInsurance().getCoverageGap());
            if (insurance.getHealthInsurance() != null)
                insMap.put("healthCoverageGap", insurance.getHealthInsurance().getCoverageGap());
            snapshot.put("insurance", insMap);

            // 6. Recurring Transactions (Budget & Investments)
            Map<String, Object> recurringMap = new HashMap<>();

            // 6.a Budget Side (Bills, Salaries, etc.)
            try {
                var budgetTemplates = budgetRecurringService.getActiveTemplates(userId);
                recurringMap.put("budgetRecurring", budgetTemplates.stream().map(t -> {
                    Map<String, Object> tMap = new HashMap<>();
                    if (t.getName() != null)
                        tMap.put("name", t.getName());
                    if (t.getType() != null)
                        tMap.put("type", t.getType());
                    if (t.getAmount() != null)
                        tMap.put("amount", t.getAmount());
                    if (t.getPattern() != null)
                        tMap.put("pattern", t.getPattern());
                    tMap.put("nextRun", t.getNextRunDate() != null ? t.getNextRunDate() : "N/A");
                    return tMap;
                }).toList());
            } catch (Exception e) {
                recurringMap.put("budgetRecurring", "Not available");
            }

            // 6.b Investment Side (SIPs, etc.)
            try {
                var investmentOps = investmentRecurringService.getActiveRecurringTransactions(userId);
                recurringMap.put("investmentRecurring", investmentOps.stream().map(t -> {
                    Map<String, Object> tMap = new HashMap<>();
                    tMap.put("name", t.getName() != null ? t.getName() : "SIP");
                    if (t.getAmount() != null)
                        tMap.put("amount", t.getAmount());
                    if (t.getFrequency() != null)
                        tMap.put("frequency", t.getFrequency());
                    if (t.getCategory() != null)
                        tMap.put("category", t.getCategory());
                    tMap.put("nextExecution", t.getNextExecutionDate() != null ? t.getNextExecutionDate() : "N/A");
                    return tMap;
                }).toList());
            } catch (Exception e) {
                recurringMap.put("investmentRecurring", "Not available");
            }
            snapshot.put("recurringTransactions", recurringMap);

        } catch (Exception e) {
            // Log error and return what we have
        }

        return snapshot;
    }
}
