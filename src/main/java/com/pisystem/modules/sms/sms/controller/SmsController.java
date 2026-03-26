package com.sms.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.security.AuthenticationHelper;
import com.sms.data.SMSImportRequest;
import com.sms.data.SMSImportResponse;
import com.sms.data.SMSTransaction;
import com.sms.service.SmsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for SMS message parsing and transaction management
 */
@RestController
@RequestMapping("api/v1/sms")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "SMS Transaction Parser", description = "APIs for parsing bank SMS messages and extracting transaction details")
public class SmsController {

    private final SmsService smsService;
    private final AuthenticationHelper authHelper;

    /**
     * Import and parse multiple SMS messages
     */
    @Operation(summary = "Import Multiple SMS Messages", description = "Parse and import multiple bank SMS messages at once. The system will extract transaction details including amount, date, merchant, account number, and more. Supports various Indian bank SMS formats (HDFC, ICICI, SBI, Axis, etc.).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SMS messages imported successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SMSImportResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request - userId or messages missing"),
            @ApiResponse(responseCode = "500", description = "Internal server error during parsing")
    })
    @PostMapping("/import")
    public ResponseEntity<SMSImportResponse> importMessages(
            @Valid @RequestBody @Parameter(description = "SMS import request with user ID and list of messages", required = true) SMSImportRequest request) {
        log.info("Received SMS import request for user {} with {} messages",
                request.getUserId(), request.getMessages().size());
        this.authHelper.validateUserAccess(request.getUserId());
        SMSImportResponse response = smsService.importMessages(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Get all SMS transactions for a user
     */
    @Operation(summary = "Get User SMS Transactions", description = "Retrieve all parsed SMS transactions for a specific user, including both processed and unprocessed transactions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved SMS transactions", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SMSTransaction.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SMSTransaction>> getUserTransactions(
            @Parameter(description = "User ID", required = true, example = "123") @PathVariable("userId") Long userId) {
        log.info("Fetching SMS transactions for user {}", userId);
        List<SMSTransaction> transactions = smsService.getUserTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get unprocessed SMS transactions for a user
     */
    @Operation(summary = "Get Unprocessed SMS Transactions", description = "Retrieve all successfully parsed SMS transactions that have not yet been converted to expenses or income. These transactions have parse_status = SUCCESS and is_processed = false.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved unprocessed transactions", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SMSTransaction.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}/unprocessed")
    public ResponseEntity<List<SMSTransaction>> getUnprocessedTransactions(
            @Parameter(description = "User ID", required = true, example = "123") @PathVariable Long userId) {
        log.info("Fetching unprocessed SMS transactions for user {}", userId);
        List<SMSTransaction> transactions = smsService.getUnprocessedTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Parse a single SMS message
     */
    @Operation(summary = "Parse Single SMS Message", description = "Parse and store a single bank SMS message. Extracts transaction details like amount, date, type (DEBIT/CREDIT), merchant, account number, balance, and more. Returns the parsed transaction with confidence score.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "SMS message parsed and saved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SMSTransaction.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request - userId or message missing"),
            @ApiResponse(responseCode = "500", description = "Internal server error during parsing")
    })
    @PostMapping("/parse")
    public ResponseEntity<SMSTransaction> parseSingleMessage(
            @Parameter(description = "Single SMS parse request", required = true) @RequestBody ParseSingleRequest request) {
        log.info("Parsing single SMS for user {}", request.getUserId());

        SMSTransaction transaction = smsService.parseSingleMessage(
                request.getUserId(),
                request.getMessage(),
                request.getSender());

        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    // Inner class for single parse request
    public static class ParseSingleRequest {
        private Long userId;
        private String message;
        private String sender;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }
    }
}
