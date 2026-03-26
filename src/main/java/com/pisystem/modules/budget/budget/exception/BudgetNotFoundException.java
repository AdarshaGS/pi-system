package com.budget.exception;

/**
 * Exception thrown when a budget limit is not found
 * Sprint 4 - Custom Exception Handling
 */
public class BudgetNotFoundException extends RuntimeException {
    
    public BudgetNotFoundException(Long id) {
        super("Budget not found with id: " + id);
    }
    
    public BudgetNotFoundException(String message) {
        super(message);
    }
}
