package com.pisystem.modules.sms.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Output DTO from the SMS classifier engine.
 *
 * <ul>
 *   <li>{@code isFinancial}     — true only for real transaction messages</li>
 *   <li>{@code messageType}     — TRANSACTION | PROMOTIONAL | OTP | SERVICE | UNKNOWN</li>
 *   <li>{@code confidenceScore} — 0-100 additive score</li>
 *   <li>{@code reason}          — human-readable explanation of the classification</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsClassificationResult {

    private boolean isFinancial;
    private MessageType messageType;
    private int confidenceScore;
    private String reason;

    public enum MessageType {
        TRANSACTION,
        PROMOTIONAL,
        OTP,
        SERVICE,
        UNKNOWN
    }
}
