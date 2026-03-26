package com.budget.exception;

/**
 * Exception thrown when an income is not found
 * Sprint 4 - Custom Exception Handling
 */
public class IncomeNotFoundException extends RuntimeException {
    
    public IncomeNotFoundException(Long id) {
        super("Income not found with id: " + id);
    }
    
    public IncomeNotFoundException(String message) {
        super(message);
    }
}
