package com.sms.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.budget.data.Expense;
import com.budget.data.Income;
import com.budget.data.TransactionType;
import com.budget.service.BudgetService;
import com.sms.data.ParsedSMSData;
import com.sms.data.SMSImportRequest;
import com.sms.data.SMSImportResponse;
import com.sms.data.SMSTemplates;
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
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public SMSImportResponse importMessages(SMSImportRequest request) {
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
                        String transactionCategory = autoCategorizeMerchant(transaction.getMerchant());
                        Expense expense = Expense.builder()
                                .userId(transaction.getUserId())
                                .amount(transaction.getAmount())
                                .expenseDate(transaction.getTransactionDate())
                                .description(transaction.getMerchant() != null ? transaction.getMerchant() : "SMS Transaction")
                                .notes("SMS Parsed - Ref: " + (transaction.getReferenceNumber() != null ? transaction.getReferenceNumber() : "N/A"))
                                .customCategoryName(transactionCategory)
                                .build();
                        this.budgetService.addExpense(expense);
                        addedToBudget = true;
                    }

                    if (transaction.getAccountNumber() != null) {
                        this.bankAccountService.addOrUpdateBankAccount(transaction.getUserId(),
                                transaction.getAccountNumber());
                    }

                    transaction.setMessageType(messageType);
                    this.repository.save(transaction);
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
        ParsedSMSData parsedData = parserService.parseSMS(message);

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

        return transaction;
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


    private String detectTransactionPatterns(SMSTransaction transaction) {
        if (transaction.getOriginalMessage() == null) {
            return "UNKNOWN";
        }

        String messageLower = transaction.getOriginalMessage().toLowerCase();

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

    private boolean containsAnyKeyword(String message, String[] keywords) {
        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAnyKeywordWithBoundary(String message, String[] keywords) {
        for (String keyword : keywords) {
            if (keyword.split("\\s+").length == 1) {
                if (message.matches(".*\\b" + keyword + "\\b.*")) {
                    return true;
                }
            } else {
                if (message.contains(keyword)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasAmount(String message) {
        return message.matches(".*(rs\\.?|inr|₹)\\s*[0-9,]+.*");
    }

    public String autoCategorizeMerchant(String merchant) {
        String category = null;

        List<String> amazonKeywords = List.of("amazon", "flipkart", "myntra", "ajio");

        if (merchant != null) {
            String merchantLower = merchant.toLowerCase();
            if (amazonKeywords.stream().anyMatch(merchantLower::contains)) {
                category = "Shopping";
            }

        }
        return category;
    }


    // public List<SMSTemplates> getSMSTemplates() {
    //     final String sql = "SELECT id, message_template FROM sms_templates";
    //     return this.jdbcTemplate.que
    //     });
    // }

}
