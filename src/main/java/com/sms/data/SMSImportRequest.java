package com.sms.data;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for importing multiple SMS messages
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for importing multiple bank SMS messages")
public class SMSImportRequest {
    
    @NotNull(message = "User ID is required")
    @Schema(description = "User ID who owns these SMS messages", example = "123")
    private Long userId;
    
    @NotEmpty(message = "At least one SMS message is required")
    @Schema(description = "List of SMS messages to parse")
    private List<SMSMessage> messages;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Individual SMS message to be parsed")
    public static class SMSMessage {
        @NotNull(message = "Message content is required")
        @Schema(
            description = "The actual SMS message content from bank/payment provider",
            example = "Rs.500 debited from A/c XX1234 on 12-03-2026 at AMAZON. Avl Bal: Rs.10,000"
        )
        private String content;
        
        @Schema(
            description = "SMS sender ID (e.g., bank name or sender code)",
            example = "HDFCBK"
        )
        private String sender; // Optional: SMS sender ID
        
        @Schema(
            description = "Unix timestamp when SMS was received (milliseconds)",
            example = "1710234567000"
        )
        private Long timestamp; // Optional: Unix timestamp when SMS was received
    }
}
