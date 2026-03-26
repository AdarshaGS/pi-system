package com.savings.exception;

import org.springframework.http.HttpStatus;

import com.common.exception.BusinessException;

/**
 * Exception thrown when a savings entity (Savings Account, Fixed Deposit, or Recurring Deposit)
 * is not found for the given ID and user.
 */
public class SavingsEntityNotFoundException extends BusinessException {

    public SavingsEntityNotFoundException(String entityType, Long id) {
        super(
            HttpStatus.NOT_FOUND,
            "SAVINGS_ENTITY_NOT_FOUND",
            String.format("%s not found for ID: %d", entityType, id)
        );
    }

    public SavingsEntityNotFoundException(String entityType, Long userId, String field) {
        super(
            HttpStatus.NOT_FOUND,
            "SAVINGS_ENTITY_NOT_FOUND",
            String.format("%s not found for user ID: %d", entityType, userId)
        );
    }
}
