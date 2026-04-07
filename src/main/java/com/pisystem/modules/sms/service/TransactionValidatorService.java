package com.pisystem.modules.sms.service;

import com.pisystem.modules.sms.data.SMSTransaction;
import com.pisystem.modules.sms.data.TransactionValidationResult;

/**
 * Contract for the transaction validation engine.
 * Validates a parsed {@link SMSTransaction} against a strict rule set
 * before it is persisted or posted to the budget.
 */
public interface TransactionValidatorService {

    /**
     * Validate a parsed SMS transaction.
     *
     * @param transaction the parsed transaction to validate
     * @return validation result with pass/fail flag and reason
     */
    TransactionValidationResult validate(SMSTransaction transaction);
}
