package com.sms.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.budget.data.Expense;
import com.budget.data.Income;
import com.budget.data.TransactionType;
import com.budget.service.BudgetService;
import com.sms.data.ParsedSMSData;
import com.sms.data.SMSImportRequest;
import com.sms.data.SMSImportResponse;
import com.sms.data.SMSTransaction;
import com.sms.repo.SMSTransactionRepository;
import com.upi.service.BankAccountService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsServiceImpl implements SmsService {

    private final SMSParserService parserService;
    private final SMSTransactionRepository repository;
    private final BudgetService budgetService;
    private final BankAccountService bankAccountService;

    @Override
    @Transactional
    public SMSImportResponse importMessages(SMSImportRequest request) {
        log.info("Importing {} SMS messages for user {}", request.getMessages().size(), request.getUserId());

        SMSImportResponse.SMSImportResponseBuilder responseBuilder = SMSImportResponse.builder();
        List<SMSImportResponse.TransactionSummary> summaries = new ArrayList<>();
        List<SMSImportResponse.ErrorDetail> errors = new ArrayList<>();

        int totalMessages = request.getMessages().size();

        for (SMSImportRequest.SMSMessage smsMessage : request.getMessages()) {
            try {
                if (repository.existsByUserIdAndOriginalMessage(request.getUserId(), smsMessage.getContent())) {
                    continue;
                }

                SMSTransaction transaction = parseSingleMessage(request.getUserId(),
                        smsMessage.getContent(), smsMessage.getSender());

                String transactionType = transaction.getTransactionType() != null
                        ? transaction.getTransactionType().name() : "UNKNOWN";

                String messageType = detectTransactionPatterns(transaction);
                boolean addedToBudget = false;

                if ("TRANSACTION".equals(messageType) && transaction.getAmount() != null) {
                    if (TransactionType.CREDIT.name().equals(transactionType)) {
                        Income income = Income.builder()
                                .userId(transaction.getUserId())
                                .amount(transaction.getAmount())
                                .date(transaction.getTransactionDate())
                                .source("SMS_PARSED")
                                .description(transaction.getMerchant() != null ? transaction.getMerchant() : "SMS Transaction")
                                .build();
                        this.budgetService.addIncome(income);
                        addedToBudget = true;
                        log.info("Created income from SMS: {}", transaction.getId());
                    } else if (TransactionType.DEBIT.name().equals(transactionType)) {
                        Expense expense = Expense.builder()
                                .userId(transaction.getUserId())
                                .amount(transaction.getAmount())
                                .expenseDate(transaction.getTransactionDate())
                                .description(transaction.getMerchant() != null ? transaction.getMerchant() : "SMS Transaction")
                                .notes("SMS Parsed - Ref: " + (transaction.getReferenceNumber() != null ? transaction.getReferenceNumber() : "N/A"))
                                .build();
                        this.budgetService.addExpense(expense);
                        addedToBudget = true;
                    }
                }

                if (transaction.getAccountNumber() != null) {
                    this.bankAccountService.addOrUpdateBankAccount(transaction.getUserId(),
                            transaction.getAccountNumber());
                }

                summaries.add(SMSImportResponse.TransactionSummary.builder()
                        .transactionId(transaction.getId())
                        .message(truncateMessage(smsMessage.getContent(), 50))
                        .status(transaction.getParseStatus())
                        .confidence(transaction.getParseConfidence())
                        .messageType(messageType)
                        .addedToBudget(addedToBudget)
                        .build());

            } catch (Exception e) {
                log.error("Error processing SMS message", e);
                errors.add(SMSImportResponse.ErrorDetail.builder()
                        .message(truncateMessage(smsMessage.getContent(), 50))
                        .error(e.getMessage())
                        .build());
            }
        }

        return responseBuilder
                .totalMessages(totalMessages)
                .transactions(summaries)
                .errors(errors)
                .build();
    }

    @Override
    @Transactional
    public SMSTransaction parseSingleMessage(Long userId, String message, String sender) {
        log.debug("Parsing single SMS for user {}", userId);

        // Parse the SMS
        ParsedSMSData parsedData = parserService.parseSMS(message);

        // Create transaction entity
        SMSTransaction transaction = SMSTransaction.builder()
                .userId(userId)
                .originalMessage(message)
                .sender(sender)
                .amount(parsedData.getAmount())
                .transactionDate(parsedData.getTransactionDate())
                .transactionTime(parsedData.getTransactionTime())
                .transactionType(parsedData.getTransactionType())
                .merchant(parsedData.getMerchant())
                .accountNumber(parsedData.getAccountNumber())
                .cardNumber(parsedData.getCardNumber())
                .balance(parsedData.getBalance())
                .referenceNumber(parsedData.getReferenceNumber())
                .upiId(parsedData.getUpiId())
                .parseStatus(parsedData.getParseStatus())
                .parseConfidence(parsedData.getConfidence())
                .errorMessage(parsedData.getErrorMessage())
                .isProcessed(false)
                .createdAt(LocalDateTime.now())
                .build();

        // Save to database
        SMSTransaction saved = repository.save(transaction);
        log.info("Saved SMS transaction with ID: {} (Status: {}, Confidence: {})",
                saved.getId(), saved.getParseStatus(), saved.getParseConfidence());

        return saved;
    }

    @Override
    public List<SMSTransaction> getUserTransactions(Long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public List<SMSTransaction> getUnprocessedTransactions(Long userId) {
        return repository.findUnprocessedSuccessfulTransactions(userId);
    }

    private String truncateMessage(String message, int maxLength) {
        if (message == null)
            return "";
        if (message.length() <= maxLength)
            return message;
        return message.substring(0, maxLength) + "...";
    }

/**
     * Detect message type from SMS transaction patterns
     * Priority order: OTP -> PROMOTIONAL -> FAILED -> MANDATE -> TRANSACTION -> BALANCE -> UNKNOWN
     * 
     * @param transaction The SMS transaction to classify
     * @return Message type: TRANSACTION, MANDATE_ALERT, FAILED_TRANSACTION, BALANCE_INQUIRY, 
     *         OTP, PROMOTIONAL, SERVICE_ALERT, or UNKNOWN
     */
    private String detectTransactionPatterns(SMSTransaction transaction) {
        if (transaction.getOriginalMessage() == null) {
            return "UNKNOWN";
        }

        String messageLower = transaction.getOriginalMessage().toLowerCase();

        // Priority 5: Future/Mandate/Scheduled Transactions (IMPORTANT!)
        String[] mandateKeywords = {
            "will be debited", "will be credited", "to be debited", "to be credited",
            "scheduled for", "scheduled on", "auto-debit on", "auto pay on",
            "autopay enabled", "mandate registered", "standing instruction",
            "subscription starts", "next debit on", "upcoming payment",
            "is scheduled", "payment due on"
        };
        if (containsAnyKeyword(messageLower, mandateKeywords)) {
            return "MANDATE_ALERT";
        }

        // Priority 6: Actual Completed Transactions (with word boundaries)
        String[] transactionKeywords = {
            "debited", "credited", "withdrawn", "deposited",
            "paid", "received", "transferred", "sent to",
            "received from", "received via", "purchase", "purchased",
            "refund credited", "cashback credited", "recharged",
            "bill payment", "emi deducted", "charged", "spent"
        };
        if (containsAnyKeywordWithBoundary(messageLower, transactionKeywords)) {
            return "TRANSACTION";
        }

        // Priority 7: Balance Inquiry Only (no transaction)
        String[] balanceKeywords = {
            "available balance", "balance enquiry", "current balance",
            "available bal", "avl bal", "balance is"
        };
        if (containsAnyKeyword(messageLower, balanceKeywords) && 
            !containsAnyKeywordWithBoundary(messageLower, transactionKeywords) &&
            !containsAnyKeyword(messageLower, mandateKeywords)) {
            return "BALANCE_INQUIRY";
        }

        return "UNKNOWN";
    }

    /**
     * Check if message contains any of the keywords
     */
    private boolean containsAnyKeyword(String message, String[] keywords) {
        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if message contains any keyword with word boundary
     * Prevents false matches like "prepaid" matching "paid"
     */
    private boolean containsAnyKeywordWithBoundary(String message, String[] keywords) {
        for (String keyword : keywords) {
            // Use word boundary regex for single words
            if (keyword.split("\\s+").length == 1) {
                if (message.matches(".*\\b" + keyword + "\\b.*")) {
                    return true;
                }
            } else {
                // For phrases, use simple contains
                if (message.contains(keyword)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if message has amount indicators
     */
    private boolean hasAmount(String message) {
        return message.matches(".*(rs\\.?|inr|₹)\\s*[0-9,]+.*");
    }

}
