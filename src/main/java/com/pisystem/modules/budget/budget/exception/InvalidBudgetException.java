package com.budget.exception;

/**
 * Exception thrown when budget operations fail validation
 * Sprint 4 - Custom Exception Handling
 */
public class InvalidBudgetException extends RuntimeException {
    
    public InvalidBudgetException(String message) {
        super(message);
    }
}
