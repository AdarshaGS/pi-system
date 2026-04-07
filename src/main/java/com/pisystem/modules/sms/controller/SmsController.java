package com.pisystem.modules.sms.controller;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pisystem.shared.security.AuthenticationHelper;
import com.pisystem.modules.sms.data.DuplicateDetectionResult;
import com.pisystem.modules.sms.data.FlowClassificationResult;
import com.pisystem.modules.sms.data.NormalizedTransaction;
import com.pisystem.modules.sms.data.RawDedupRequest;
import com.pisystem.modules.sms.data.RawSmsDuplicateResult;
import com.pisystem.modules.sms.data.SMSImportRequest;
import com.pisystem.modules.sms.data.SMSImportResponse;
import com.pisystem.modules.sms.data.SMSTransaction;
import com.pisystem.modules.sms.data.SmsClassificationResult;
import com.pisystem.modules.sms.data.TransactionValidationResult;
import com.pisystem.modules.sms.data.TransferDetectionResult;
import com.pisystem.modules.sms.service.DuplicateDetectionService;
import com.pisystem.modules.sms.service.RawSmsDeduplicationService;
import com.pisystem.modules.sms.service.SmsClassifierService;
import com.pisystem.modules.sms.service.SmsService;
import com.pisystem.modules.sms.service.TransactionNormalizationService;
import com.pisystem.modules.sms.service.TransactionValidatorService;
import com.pisystem.modules.sms.service.SmsPatternRegistry;
import com.pisystem.modules.sms.service.TransactionFlowClassifierService;
import com.pisystem.modules.sms.service.TransferDetectionService;

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
    private final TransactionNormalizationService normalizationService;
    private final DuplicateDetectionService duplicateDetectionService;
    private final SmsClassifierService classifierService;
    private final TransactionValidatorService validatorService;
    private final RawSmsDeduplicationService rawDedupService;
    private final SmsPatternRegistry patternRegistry;
    private final TransactionFlowClassifierService flowClassifierService;
    private final TransferDetectionService transferDetectionService;

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

    /**
     * Parse a single SMS and immediately normalize it.
     * Does NOT persist to the database — useful for previewing how the engine
     * will categorize a given message before bulk import.
     */
    @Operation(
            summary = "Normalize a Single SMS",
            description = "Parse and normalize a raw bank SMS message. Returns a structured NormalizedTransaction " +
                    "with cleaned merchant name, category, payment mode, flow type, and a 0-100 confidence score. " +
                    "This endpoint does NOT persist any data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SMS normalized successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NormalizedTransaction.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/normalize")
    public ResponseEntity<NormalizedTransaction> normalizeSingleMessage(
            @RequestBody ParseSingleRequest request) {
        authHelper.validateUserAccess(request.getUserId());
        log.info("Normalize request for user {}", request.getUserId());

        // Parse then normalize — no DB write
        SMSTransaction transaction = smsService.parseSingleMessage(
                request.getUserId(), request.getMessage(), request.getSender());
        NormalizedTransaction normalized = normalizationService.normalize(transaction, request.getMessage());

        return ResponseEntity.ok(normalized);
    }

    /**
     * Check whether a single SMS would be flagged as a duplicate if imported.
     * Parses the SMS, then runs the duplicate-detection engine against the user's
     * existing transaction history. Does NOT persist any data.
     */
    @Operation(
            summary = "Check SMS for Duplicate",
            description = "Parse a raw SMS and check whether an identical or near-identical transaction " +
                    "already exists in the user's history. Returns isDuplicate, confidence score, and reason. " +
                    "No data is written to the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Duplicate check completed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DuplicateDetectionResult.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/check-duplicate")
    public ResponseEntity<DuplicateDetectionResult> checkDuplicate(
            @RequestBody ParseSingleRequest request) {
        authHelper.validateUserAccess(request.getUserId());
        log.info("Duplicate check request for user {}", request.getUserId());

        SMSTransaction candidate = smsService.parseSingleMessage(
                request.getUserId(), request.getMessage(), request.getSender());
        DuplicateDetectionResult result = duplicateDetectionService.detectWithDbLookup(candidate);
        return ResponseEntity.ok(result);
    }

    /**
     * Classify a raw SMS as TRANSACTION, PROMOTIONAL, OTP, SERVICE, or UNKNOWN.
     * No data is written to the database.
     */
    @Operation(
            summary = "Classify an SMS",
            description = "Determine whether a raw SMS is a financial transaction, promotional, OTP, " +
                    "service message, or unknown. Returns isFinancial flag, messageType, " +
                    "0-100 confidence score, and a human-readable reason. No data is persisted.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Classification completed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SmsClassificationResult.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/classify")
    public ResponseEntity<SmsClassificationResult> classifySms(
            @RequestBody ClassifyRequest request) {
        log.info("Classify request received");
        SmsClassificationResult result = classifierService.classify(request.getMessage());
        return ResponseEntity.ok(result);
    }

    /**
     * Parse a single SMS and immediately validate it as a clean transaction.
     * No data is written to the database.
     */
    @Operation(
            summary = "Validate a Parsed Transaction",
            description = "Parse a raw bank SMS and run it through the transaction validation engine. " +
                    "Returns isValidTransaction and a reason string. " +
                    "Rejects messages with missing amount, unknown type, low confidence, or partial parse. " +
                    "No data is persisted.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validation completed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransactionValidationResult.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/validate")
    public ResponseEntity<TransactionValidationResult> validateTransaction(
            @RequestBody ParseSingleRequest request) {
        authHelper.validateUserAccess(request.getUserId());
        log.info("Validate request for user {}", request.getUserId());

        SMSTransaction transaction = smsService.parseSingleMessage(
                request.getUserId(), request.getMessage(), request.getSender());
        TransactionValidationResult result = validatorService.validate(transaction);
        return ResponseEntity.ok(result);
    }

    // Inner class for single parse request
    public static class ParseSingleRequest {
        private Long userId;
        private String message;
        private String sender;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }
    }

    /**
     * Deduplicate a batch of raw SMS messages before parsing.
     *
     * <p>Each message is evaluated against earlier messages in the same batch
     * and against the user's persisted transaction history.  Two rules apply
     * (first match wins):
     * <ol>
     *   <li><b>Exact match (100 %)</b> – same sender + identical body.</li>
     *   <li><b>Near match (90 %)</b>  – same sender + body similarity ≥ 90 %
     *       + timestamp difference &lt; 30 seconds.</li>
     * </ol>
     * No data is written to the database.
     */
    @Operation(
            summary = "Raw SMS Batch Deduplication",
            description = "Check a batch of raw SMS messages for duplicates — both within the batch and "
                    + "against the user's existing records — without parsing financial data. "
                    + "Returns one result per message with isDuplicate, duplicateOf, confidenceScore, and reason.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deduplication results returned",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RawSmsDuplicateResult.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/raw-dedup")
    public ResponseEntity<List<RawSmsDuplicateResult>> rawDedup(
            @Valid @RequestBody RawDedupRequest request) {
        authHelper.validateUserAccess(request.getUserId());
        log.info("raw-dedup request: userId={} messages={}",
                request.getUserId(), request.getMessages().size());
        List<RawSmsDuplicateResult> results = rawDedupService.checkBatch(request);
        return ResponseEntity.ok(results);
    }

    // Inner class for classify request (no userId needed — classification is stateless)
    public static class ClassifyRequest {
        private String message;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /**
     * Reload SMS regex patterns from the database without restarting the server.
     * Call this after inserting or updating a row in {@code sms_regex_patterns}.
     */
    @Operation(
            summary = "Refresh SMS Regex Patterns",
            description = "Reload all active regex patterns from the sms_regex_patterns table into the " +
                    "in-memory cache. Call this after editing patterns in the DB — no restart required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Patterns reloaded successfully"),
            @ApiResponse(responseCode = "500", description = "Error during reload")
    })
    @PostMapping("/patterns/refresh")
    public ResponseEntity<String> refreshPatterns() {
        patternRegistry.refresh();
        log.info("SMS regex patterns refreshed via API. Loaded: {} patterns", patternRegistry.size());
        return ResponseEntity.ok("Patterns refreshed. Loaded: " + patternRegistry.size());
    }

    /**
     * Parse and normalize a single SMS, then classify its flow type.
     * Returns INCOME, EXPENSE, TRANSFER, or IGNORE with confidence score.
     * No data is written to the database.
     */
    @Operation(
            summary = "Classify Transaction Flow",
            description = "Parse and normalize a raw bank SMS, then run it through the flow classifier. " +
                    "Returns INCOME, EXPENSE, TRANSFER, or IGNORE with a 0-100 confidence score " +
                    "and the reason for the decision. No data is persisted.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Flow classification completed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FlowClassificationResult.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/classify-flow")
    public ResponseEntity<FlowClassificationResult> classifyFlow(
            @RequestBody ParseSingleRequest request) {
        authHelper.validateUserAccess(request.getUserId());
        log.info("Classify-flow request for user {}", request.getUserId());

        SMSTransaction transaction = smsService.parseSingleMessage(
                request.getUserId(), request.getMessage(), request.getSender());
        NormalizedTransaction normalized = normalizationService.normalize(transaction, request.getMessage());
        FlowClassificationResult result = flowClassifierService.classify(normalized);
        return ResponseEntity.ok(result);
    }

    /**
     * Detect whether a single SMS represents a fund transfer (own-account, paired, or UPI self-transfer).
     * No data is written to the database.
     */
    @Operation(
            summary = "Detect Transfer",
            description = "Parse a raw bank SMS and run it through the transfer detection engine. "
                    + "Returns isTransfer, fromAccount, toAccount, confidenceScore (0-100), and reason. "
                    + "Covers own-account, paired DEBIT/CREDIT, and UPI self-transfer signals. "
                    + "No data is persisted.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer detection completed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransferDetectionResult.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/detect-transfer")
    public ResponseEntity<TransferDetectionResult> detectTransfer(
            @RequestBody ParseSingleRequest request) {
        authHelper.validateUserAccess(request.getUserId());
        log.info("Detect-transfer request for user {}", request.getUserId());

        SMSTransaction transaction = smsService.parseSingleMessage(
                request.getUserId(), request.getMessage(), request.getSender());

        // Use recent history (±1 day) for the paired-transaction rule
        LocalDate anchor = LocalDate.now();
        List<SMSTransaction> recentHistory =
                smsService.getTransactionsInWindow(request.getUserId(),
                        anchor.minusDays(1), anchor.plusDays(1));

        Set<String> userAccountNos =
                new HashSet<>(smsService.getUserBankAccountNumbers(request.getUserId()));

        TransferDetectionResult result =
                transferDetectionService.detect(transaction, userAccountNos,
                        Set.of(), recentHistory);
        return ResponseEntity.ok(result);
    }
}
