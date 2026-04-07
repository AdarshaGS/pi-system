package com.pisystem.modules.sms.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Output DTO from the transaction validation engine.
 *
 * <ul>
 *   <li>{@code isValidTransaction} — true only when the transaction is clean and complete</li>
 *   <li>{@code reason}             — human-readable explanation of the decision</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionValidationResult {

    private boolean isValidTransaction;
    private String reason;
}
