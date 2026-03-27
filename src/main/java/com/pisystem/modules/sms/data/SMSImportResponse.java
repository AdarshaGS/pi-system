package com.pisystem.modules.sms.data;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for SMS import operation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing results of SMS import operation")
public class SMSImportResponse {
    
    @Schema(description = "Total number of SMS messages in the import request", example = "10")
    private int totalMessages;
    
    @Builder.Default
    @Schema(description = "List of parsed transaction summaries")
    private List<TransactionSummary> transactions = new ArrayList<>();
    
    @Builder.Default
    @Schema(description = "List of errors encountered during parsing")
    private List<ErrorDetail> errors = new ArrayList<>();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Summary of a parsed transaction")
    public static class TransactionSummary {
        @Schema(description = "Database ID of the created transaction", example = "101")
        private Long transactionId;
        
        @Schema(description = "Truncated SMS message (first 50 characters)", example = "Rs.500 debited from A/c XX1234...")
        private String message;
        
        @Schema(description = "Parse status: SUCCESS, PARTIAL, or FAILED", example = "SUCCESS")
        private SMSTransaction.ParseStatus status;
        
        @Schema(description = "Confidence score (0.0 to 1.0)", example = "0.95")
        private Double confidence;
        
        @Schema(description = "Message type: TRANSACTION, MANDATE_ALERT, OTP, PROMOTIONAL, etc.", example = "TRANSACTION")
        private String messageType;
        
        @Schema(description = "Whether this was added to budget", example = "true")
        private boolean addedToBudget;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Error detail for a failed parse")
    public static class ErrorDetail {
        @Schema(description = "The SMS message that failed to parse")
        private String message;
        
        @Schema(description = "Error description")
        private String error;
    }
}
