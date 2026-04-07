package com.pisystem.modules.sms.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Final authoritative classification of a normalized SMS transaction.
 *
 * <ul>
 *   <li>{@code INCOME}   — money arriving in the user's account (CREDIT)</li>
 *   <li>{@code EXPENSE}  — money leaving the user's account (DEBIT to a third party)</li>
 *   <li>{@code TRANSFER} — money moving between the user's own accounts</li>
 *   <li>{@code IGNORE}   — no amount, invalid status, or non-financial message</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowClassificationResult {

    private FlowType flowType;
    private int confidenceScore;
    private String reason;

    public enum FlowType {
        INCOME,
        EXPENSE,
        TRANSFER,
        IGNORE
    }
}
