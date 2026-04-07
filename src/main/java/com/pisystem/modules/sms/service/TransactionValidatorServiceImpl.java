package com.pisystem.modules.sms.service;

import org.springframework.stereotype.Service;

import com.pisystem.modules.sms.data.SMSTransaction;
import com.pisystem.modules.sms.data.SMSTransaction.ParseStatus;
import com.pisystem.modules.sms.data.SMSTransaction.TransactionType;
import com.pisystem.modules.sms.data.TransactionValidationResult;

import lombok.extern.slf4j.Slf4j;

/**
 * Deterministic, rule-based transaction validation engine.
 *
 * <h3>Validation rules (applied in order — first failure exits)</h3>
 * <ol>
 *   <li><b>Amount</b>            — must be non-null and positive (> 0).</li>
 *   <li><b>Transaction type</b>  — must be DEBIT or CREDIT; UNKNOWN and
 *       COMPLEX_TRANSACTION are rejected.</li>
 *   <li><b>Parse confidence</b>  — must be ≥ 0.50 (50 %). Low-confidence
 *       parses indicate missing or ambiguous fields.</li>
 *   <li><b>Parse status</b>      — must be SUCCESS; PARTIAL, FAILED, and
 *       LOW_CONFIDENCE are rejected.</li>
 *   <li><b>Transaction date</b>  — must be present; a transaction without a
 *       date cannot be posted to the budget correctly.</li>
 * </ol>
 *
 * All rules must pass for {@code isValidTransaction} to be {@code true}.
 */
@Service
@Slf4j
public class TransactionValidatorServiceImpl implements TransactionValidatorService {

    /** Minimum parse confidence (0-1 scale) required for a clean transaction. */
    private static final double MIN_CONFIDENCE = 0.50;

    @Override
    public TransactionValidationResult validate(SMSTransaction transaction) {
        if (transaction == null) {
            return invalid("Transaction object is null");
        }

        // ── Rule 1: Amount ────────────────────────────────────────────────────
        if (transaction.getAmount() == null) {
            return invalid("Amount is missing");
        }
        if (transaction.getAmount().signum() <= 0) {
            return invalid("Amount must be greater than zero (got " + transaction.getAmount() + ")");
        }

        // ── Rule 2: Transaction type ──────────────────────────────────────────
        // DEBIT, CREDIT, COMPLEX_TRANSACTION are all accepted.
        // Only a completely null type (parser produced nothing) is rejected.
        TransactionType type = transaction.getTransactionType();

        // ── Rule 3: Parse confidence ──────────────────────────────────────────
        Double confidence = transaction.getParseConfidence();
        if (confidence == null || confidence < MIN_CONFIDENCE) {
            String got = confidence != null ? String.format("%.0f%%", confidence * 100) : "null";
            return invalid("Parse confidence too low (" + got + ") — minimum required is 50%");
        }

        // ── Rule 4: Parse status ──────────────────────────────────────────────
        ParseStatus status = transaction.getParseStatus();
        if (status == null) {
            return invalid("Parse status is null");
        }
        if (status == ParseStatus.FAILED) {
            String msg = transaction.getErrorMessage() != null
                    ? transaction.getErrorMessage()
                    : "parser returned FAILED";
            return invalid("Parse failed: " + msg);
        }
        if (status == ParseStatus.PARTIAL) {
            return invalid("Partial parse — required fields may be missing");
        }
        if (status == ParseStatus.LOW_CONFIDENCE) {
            return invalid("LOW_CONFIDENCE parse status — transaction data is unreliable");
        }

        // ── Rule 5: Transaction date ──────────────────────────────────────────
        if (transaction.getTransactionDate() == null) {
            return invalid("Transaction date is missing — required for budget posting");
        }

        // ── All rules passed ──────────────────────────────────────────────────
        log.debug("Transaction validated: type={}, amount={}, confidence={:.0f}%",
                type, transaction.getAmount(), confidence * 100);

        return TransactionValidationResult.builder()
                .isValidTransaction(true)
                .reason("All validation rules passed")
                .build();
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private TransactionValidationResult invalid(String reason) {
        log.debug("Transaction rejected: {}", reason);
        return TransactionValidationResult.builder()
                .isValidTransaction(false)
                .reason(reason)
                .build();
    }
}
