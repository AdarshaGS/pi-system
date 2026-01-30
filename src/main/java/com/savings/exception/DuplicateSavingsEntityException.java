package com.savings.exception;

import org.springframework.http.HttpStatus;

import com.common.exception.BusinessException;

/**
 * Exception thrown when attempting to create a duplicate savings entity
 * (Savings Account, Fixed Deposit, or Recurring Deposit) for the same user and bank combination.
 */
public class DuplicateSavingsEntityException extends BusinessException {

    public DuplicateSavingsEntityException(String entityType, String bankName) {
        super(
            HttpStatus.CONFLICT,
            "DUPLICATE_SAVINGS_ENTITY",
            String.format("%s already exists for this user and bank '%s'. Please use a different bank or update the existing record.", 
                entityType, bankName)
        );
    }

    public DuplicateSavingsEntityException(String message) {
        super(HttpStatus.CONFLICT, "DUPLICATE_SAVINGS_ENTITY", message);
    }
}
