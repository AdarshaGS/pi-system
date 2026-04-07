package com.pisystem.modules.sms.data;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for the raw-SMS deduplication endpoint.
 *
 * <p>Each message carries a client-assigned {@code messageId} that is echoed
 * back in every {@link RawSmsDuplicateResult} so callers can correlate
 * results without relying on list order.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Batch of raw SMS messages to check for duplicates")
public class RawDedupRequest {

    @NotNull(message = "userId is required for database duplicate checks")
    @Schema(description = "Owner of the messages; used for DB-level dedup", example = "42")
    private Long userId;

    @Valid
    @NotEmpty(message = "At least one message is required")
    @Schema(description = "Messages to evaluate — each receives one result entry")
    private List<RawSmsMessage> messages;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "A single raw SMS message in the dedup request")
    public static class RawSmsMessage {

        /**
         * Client-assigned identifier used to correlate this message with its
         * result.  Any non-blank string works (UUID, sequential index, etc.).
         */
        @NotBlank(message = "messageId must not be blank")
        @Schema(description = "Client-assigned ID echoed in the response", example = "msg-001")
        private String messageId;

        @Schema(
            description = "SMS sender ID as it appears on the device (e.g. bank short code)",
            example = "HDFCBK"
        )
        private String sender;

        @NotBlank(message = "body must not be blank")
        @Schema(
            description = "Raw SMS body text, exactly as received",
            example = "Rs.500 debited from A/c XX1234 on 01-04-2026. Avl Bal: Rs.9,500"
        )
        private String body;

        /**
         * Unix epoch in milliseconds when the SMS was received.
         * Required for near-match (Rule 2) evaluation; omitting it disables
         * the 30-second timestamp window check.
         */
        @Schema(
            description = "Unix timestamp in ms when the SMS arrived on the device",
            example = "1743494400000"
        )
        private Long timestamp;
    }
}
