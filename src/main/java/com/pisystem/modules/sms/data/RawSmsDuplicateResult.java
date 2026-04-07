package com.pisystem.modules.sms.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Decision returned by the raw-SMS deduplication engine for a single message.
 *
 * <h3>Confidence bands</h3>
 * <ul>
 *   <li><b>100</b> – Exact duplicate: same sender + identical body.</li>
 *   <li><b>90</b>  – Near duplicate: same sender + body similarity ≥ 90 %
 *                    + timestamp within 30 seconds.</li>
 *   <li><b>0</b>   – Not a duplicate.</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Deduplication verdict for a single raw SMS message")
public class RawSmsDuplicateResult {

    /**
     * Echoes the {@code messageId} from the request so callers can correlate
     * results without relying on list position.
     */
    @Schema(description = "Echoed messageId from the request", example = "msg-001")
    private String messageId;

    @Schema(description = "True when the message is considered a duplicate", example = "true")
    private boolean isDuplicate;

    /**
     * Identifies what this message duplicates.
     * <ul>
     *   <li>When the duplicate was found <em>in the same batch</em>: the
     *       {@code messageId} of the earlier occurrence.</li>
     *   <li>When found <em>in the database</em>: the string representation of
     *       the persisted {@code SMSTransaction.id}.</li>
     *   <li>{@code null} when {@code isDuplicate} is {@code false}.</li>
     * </ul>
     */
    @Schema(
        description = "messageId (in-batch) or DB transaction id (string) of the original message; null if not a duplicate",
        example = "msg-000",
        nullable = true
    )
    private String duplicateOf;

    /** Confidence that the match is a true duplicate, 0–100. */
    @Schema(description = "Confidence score: 100 = exact, 90 = near, 0 = not duplicate", example = "100")
    private int confidenceScore;

    /** Human-readable explanation of the match decision. */
    @Schema(
        description = "Reason for the decision",
        example = "Exact duplicate in batch (same sender and body)"
    )
    private String reason;
}
