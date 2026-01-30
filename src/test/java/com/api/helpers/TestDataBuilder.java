package com.api.helpers;

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
        user.put("firstName", "Test");
        user.put("lastName", "User");
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
}
