package com.pisystem.modules.sms.data;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Normalized output of a raw SMS transaction after running through the
 * TransactionNormalizationService. Provides clean, structured, deterministic
 * fields suitable for downstream budget/expense/income recording.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NormalizedTransaction {

    // ── Core ──────────────────────────────────────────────────────────────────

    private BigDecimal amount;

    /** DEBIT or CREDIT as extracted from SMS */
    private String transactionType;

    /** EXPENSE, INCOME, TRANSFER, SELF_TRANSFER, UNKNOWN */
    private String flowType;

    // ── Merchant ──────────────────────────────────────────────────────────────

    /** Raw merchant string from the SMS (e.g. "UPI-SWIGGY") */
    private String merchantName;

    /** Human-readable cleaned name (e.g. "Swiggy") */
    private String normalizedMerchant;

    /** Inferred spend category (e.g. "Food", "Travel", "Shopping") */
    private String category;

    // ── Account / Mode ────────────────────────────────────────────────────────

    /** Masked account / card number from SMS */
    private String account;

    /** UPI, ATM, CARD, NETBANKING, UNKNOWN */
    private String mode;

    // ── Reference ─────────────────────────────────────────────────────────────

    /** UPI ref, RRN, UTR, or generic reference number; null if absent */
    private String referenceId;

    // ── Time ──────────────────────────────────────────────────────────────────

    /** ISO date string (yyyy-MM-dd) from the SMS or system date */
    private String timestamp;

    // ── Status ────────────────────────────────────────────────────────────────

    /** SUCCESS, FAILED, PENDING */
    private String status;

    // ── Quality ───────────────────────────────────────────────────────────────

    /**
     * Normalization confidence on a 0–100 scale.
     * 80–100 = strong match, 50–79 = partial, < 50 = low confidence.
     */
    private int confidenceScore;

    // ── Enums ─────────────────────────────────────────────────────────────────

    public enum TransactionType {
        DEBIT, CREDIT
    }

    public enum FlowType {
        EXPENSE, INCOME, TRANSFER, SELF_TRANSFER, UNKNOWN
    }

    public enum PaymentMode {
        UPI, ATM, CARD, NETBANKING, UNKNOWN
    }

    public enum TransactionStatus {
        SUCCESS, FAILED, PENDING
    }
}
