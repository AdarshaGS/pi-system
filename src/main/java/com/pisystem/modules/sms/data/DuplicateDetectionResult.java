package com.pisystem.modules.sms.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result returned by the duplicate-detection engine for a single candidate
 * transaction.
 *
 * <p>Three tiers of confidence:
 * <ul>
 *   <li><b>95–100</b> – Strong match (same amount + type + account + referenceId)</li>
 *   <li><b>80–94</b>  – Medium match (same amount + merchant + account, ≤ 2 min apart)</li>
 *   <li><b>60–79</b>  – Weak / possible duplicate (same amount, ≤ 1 min apart)</li>
 *   <li><b>0–59</b>   – Not a duplicate</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateDetectionResult {

    /** Whether the incoming transaction is considered a duplicate. */
    private boolean isDuplicate;

    /**
     * Database ID of the existing transaction that was matched, or {@code null}
     * when no duplicate was found.
     */
    private Long matchedTransactionId;

    /**
     * Confidence that the match is a true duplicate, expressed as an integer on
     * the 0–100 scale.
     */
    private int confidenceScore;

    /** Human-readable explanation of the match decision. */
    private String reason;

    /** The tier that produced the match: STRONG, MEDIUM, WEAK, or NONE. */
    private MatchTier matchTier;

    public enum MatchTier {
        /** All four fields (amount, type, account, referenceId) agree. */
        STRONG,
        /** Amount + merchant + account agree and timestamps are ≤ 2 min apart. */
        MEDIUM,
        /** Amount matches and timestamps are ≤ 1 min apart. */
        WEAK,
        /** No duplicate detected. */
        NONE
    }
}
