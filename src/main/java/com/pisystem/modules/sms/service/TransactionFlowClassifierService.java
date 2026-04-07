package com.pisystem.modules.sms.service;

import java.util.Set;

import com.pisystem.modules.sms.data.FlowClassificationResult;
import com.pisystem.modules.sms.data.NormalizedTransaction;

/**
 * Final-stage classifier that collapses a {@link NormalizedTransaction} into
 * one of four actionable flow types: INCOME, EXPENSE, TRANSFER, or IGNORE.
 */
public interface TransactionFlowClassifierService {

    /**
     * Classify without user-account context (no TRANSFER override).
     *
     * @param normalized the normalized transaction to classify
     * @return classification result
     */
    FlowClassificationResult classify(NormalizedTransaction normalized);

    /**
     * Classify with user-account context so inter-account transfers can be
     * detected.
     *
     * @param normalized      the normalized transaction to classify
     * @param userAccountNos  masked account numbers belonging to the user
     *                        (e.g. {@code Set.of("XXXX1234", "XXXX5678")})
     * @return classification result
     */
    FlowClassificationResult classify(NormalizedTransaction normalized, Set<String> userAccountNos);
}
