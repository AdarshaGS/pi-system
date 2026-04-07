package com.pisystem.modules.sms.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of the transfer detection engine for a single SMS transaction.
 *
 * <ul>
 *   <li>{@code isTransfer}      — true when any transfer rule matched</li>
 *   <li>{@code fromAccount}     — masked source account (e.g. XXXX1234), null if unknown</li>
 *   <li>{@code toAccount}       — masked destination account, null if unknown</li>
 *   <li>{@code confidenceScore} — 0-100 additive score</li>
 *   <li>{@code reason}          — human-readable explanation of the decision</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferDetectionResult {

    private boolean isTransfer;
    private String fromAccount;
    private String toAccount;
    private int confidenceScore;
    private String reason;
}
