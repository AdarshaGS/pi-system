package com.api.helpers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to build test data for API tests
 */
public class TestDataBuilder {

    /**
     * Generate unique email for testing
     */
    public static String generateUniqueEmail() {
        return "test" + System.currentTimeMillis() + "@example.com";
    }

    /**
     * Generate test user data
     */
    public static Map<String, Object> createTestUser() {
        Map<String, Object> user = new HashMap<>();
        user.put("email", generateUniqueEmail());
        user.put("password", "Test@1234");
        user.put("name", "Test User");
        user.put("mobileNumber", "9876543210");
        return user;
    }

    /**
     * Create portfolio data
     */
    public static Map<String, Object> createPortfolioData(Long userId, String symbol, int quantity, double buyPrice) {
        Map<String, Object> portfolio = new HashMap<>();
        portfolio.put("userId", userId);
        portfolio.put("symbol", symbol);
        portfolio.put("quantity", quantity);
        portfolio.put("buyPrice", buyPrice);
        portfolio.put("buyDate", "2024-01-15");
        return portfolio;
    }

    /**
     * Create savings account data
     */
    public static Map<String, Object> createSavingsAccountData(Long userId, String bankName, double amount) {
        Map<String, Object> savings = new HashMap<>();
        savings.put("userId", userId);
        savings.put("accountHolderName", "Test User");
        savings.put("bankName", bankName);
        savings.put("amount", amount);
        return savings;
    }

    /**
     * Create expense data
     */
    public static Map<String, Object> createExpenseData(Long userId, String category, double amount) {
        Map<String, Object> expense = new HashMap<>();
        expense.put("userId", userId);
        expense.put("category", category);
        expense.put("amount", amount);
        expense.put("description", "Test expense");
        expense.put("date", "2024-01-15");
        return expense;
    }

    /**
     * Create loan data
     */
    public static Map<String, Object> createLoanData(Long userId, String loanType, double amount) {
        Map<String, Object> loan = new HashMap<>();
        loan.put("userId", userId);
        loan.put("loanType", loanType);
        loan.put("principalAmount", amount);
        loan.put("interestRate", 8.5);
        loan.put("tenureMonths", 240);
        loan.put("emi", 5000);
        loan.put("startDate", "2024-01-01");
        return loan;
    }

    /**
     * Create lending data
     */
    public static Map<String, Object> createLendingData(Long userId, String borrowerName, double amount) {
        Map<String, Object> lending = new HashMap<>();
        lending.put("userId", userId);
        lending.put("borrowerName", borrowerName);
        lending.put("amount", amount);
        lending.put("interestRate", 8.5);
        lending.put("lendingDate", LocalDate.now().toString());
        lending.put("dueDate", LocalDate.now().plusMonths(6).toString());
        lending.put("status", "ACTIVE");
        return lending;
    }

    /**
     * Create insurance data
     */
    public static Map<String, Object> createInsuranceData(Long userId, String policyType, double coverageAmount) {
        Map<String, Object> insurance = new HashMap<>();
        insurance.put("userId", userId);
        insurance.put("insuranceType", policyType);
        insurance.put("policyNumber", "POL" + System.currentTimeMillis());
        insurance.put("provider", "Test Insurance Co");
        insurance.put("coverageAmount", coverageAmount);
        insurance.put("premiumAmount", coverageAmount * 0.02);
        insurance.put("premiumFrequency", "ANNUAL");
        insurance.put("startDate", LocalDate.now().toString());
        insurance.put("endDate", LocalDate.now().plusYears(1).toString());
        insurance.put("status", "ACTIVE");
        return insurance;
    }

    /**
     * Create tax data
     */
    public static Map<String, Object> createTaxData(Long userId, String financialYear) {
        Map<String, Object> tax = new HashMap<>();
        tax.put("userId", userId);
        tax.put("financialYear", financialYear);
        tax.put("grossIncome", 1200000.0);
        tax.put("deductions80C", 150000.0);
        tax.put("deductions80D", 25000.0);
        tax.put("taxRegime", "OLD");
        return tax;
    }

    /**
     * Create capital gains transaction data
     */
    public static Map<String, Object> createCapitalGainsData(Long userId, String assetType) {
        Map<String, Object> cg = new HashMap<>();
        cg.put("userId", userId);
        cg.put("assetType", assetType);
        cg.put("assetName", "RELIANCE");
        cg.put("purchaseDate", LocalDate.now().minusYears(2).toString());
        cg.put("saleDate", LocalDate.now().toString());
        cg.put("purchasePrice", 100000.0);
        cg.put("salePrice", 120000.0);
        cg.put("quantity", 10);
        return cg;
    }

    /**
     * Create TDS entry data
     */
    public static Map<String, Object> createTDSData(Long userId, String financialYear) {
        Map<String, Object> tds = new HashMap<>();
        tds.put("userId", userId);
        tds.put("financialYear", financialYear);
        tds.put("quarter", "Q1");
        tds.put("deductorName", "ABC Company");
        tds.put("deductorTAN", "ABCD12345E");
        tds.put("tdsAmount", 25000.0);
        tds.put("deductionDate", LocalDate.now().toString());
        return tds;
    }

    /**
     * Create stock data
     */
    public static Map<String, Object> createStockData(String symbol, String companyName) {
        Map<String, Object> stock = new HashMap<>();
        stock.put("symbol", symbol);
        stock.put("companyName", companyName);
        stock.put("exchange", "NSE");
        stock.put("sector", "Technology");
        stock.put("industry", "IT Services");
        stock.put("currentPrice", 3500.0);
        return stock;
    }
}
