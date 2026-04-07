package com.pisystem.modules.sms.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pisystem.modules.budget.data.Expense;
import com.pisystem.modules.budget.data.ExpenseCategory;
import com.pisystem.modules.budget.data.Income;
import com.pisystem.modules.budget.service.BudgetService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.pisystem.modules.sms.data.DuplicateDetectionResult;
import com.pisystem.modules.sms.data.FlowClassificationResult;
import com.pisystem.modules.sms.data.FlowClassificationResult.FlowType;
import com.pisystem.modules.sms.data.NormalizedTransaction;
import com.pisystem.modules.sms.data.ParsedSMSData;
import com.pisystem.modules.sms.data.SMSImportRequest;
import com.pisystem.modules.sms.data.SMSImportResponse;
import com.pisystem.modules.sms.data.SMSTransaction;
import com.pisystem.modules.sms.data.SMSTransaction.ParseStatus;
import com.pisystem.modules.sms.data.TransactionValidationResult;
import com.pisystem.modules.sms.data.TransferDetectionResult;
import com.pisystem.modules.sms.repo.SMSTransactionRepository;
import com.pisystem.modules.upi.repository.BankAccountRepository;
import com.pisystem.modules.upi.service.BankAccountService;

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
    private final BankAccountRepository bankAccountRepository;
    private final TransactionNormalizationService normalizationService;
    private final DuplicateDetectionService duplicateDetectionService;
    private final TransactionValidatorService validatorService;
    private final TransactionFlowClassifierService flowClassifierService;
    private final TransferDetectionService transferDetectionService;

    private final Cache<Long, Set<String>> userBankAccountsCache = Caffeine.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .maximumSize(1000)
        .build();

    @Override
    @Transactional
    public SMSImportResponse importMessages(SMSImportRequest request) {
        SMSImportResponse.SMSImportResponseBuilder responseBuilder = SMSImportResponse.builder();
        List<SMSImportResponse.TransactionSummary> summaries = new ArrayList<>();
        List<SMSImportResponse.ErrorDetail> errors = new ArrayList<>();
        int totalMessages = request.getMessages().size();

        List<String> messageContents = request.getMessages().stream()
            .map(SMSImportRequest.SMSMessage::getContent)
            .collect(Collectors.toList());
        
        Set<String> existingMessages = new HashSet<>(
            repository.findExistingMessages(request.getUserId(), messageContents)
        );
        
        Set<String> userBankAccounts = getUserBankAccounts(request.getUserId());

        // ── Pre-load duplicate-detection window (covers today ± 1 day) ──────────
        // Re-using one batch load for all messages is far cheaper than one DB hit
        // per transaction.
        LocalDate anchor = LocalDate.now();
        List<SMSTransaction> dupWindow = repository.findSuccessTransactionsInWindow(
                request.getUserId(),
                anchor.minusDays(1),
                anchor.plusDays(1));
        
        List<SMSTransaction> transactionsToSave = new ArrayList<>();
        List<Income> incomesToSave = new ArrayList<>();
        List<Expense> expensesToSave = new ArrayList<>();
        // Parallel index lists: each entry maps income/expense[j] → transactionsToSave[idx]
        List<Integer> incomeTxIndices = new ArrayList<>();
        List<Integer> expenseTxIndices = new ArrayList<>();

        for (SMSImportRequest.SMSMessage smsMessage : request.getMessages()) {
            try {
                if (existingMessages.contains(smsMessage.getContent())) {
                    continue;
                }

                SMSTransaction transaction = parseSingleMessage(request.getUserId(),
                        smsMessage.getContent(), smsMessage.getSender());

                // ── Duplicate detection ────────────────────────────────────────
                if ("TRANSACTION".equals(detectTransactionPatterns(transaction))
                        && transaction.getAmount() != null) {
                    DuplicateDetectionResult dupResult =
                            duplicateDetectionService.detect(transaction, dupWindow);
                    if (dupResult.isDuplicate()) {
                        log.info("Skipping duplicate transaction for user {}: {} [confidence={}, reason={}]",
                                request.getUserId(), smsMessage.getContent(),
                                dupResult.getConfidenceScore(), dupResult.getReason());
                        summaries.add(SMSImportResponse.TransactionSummary.builder()
                                .message(truncateMessage(smsMessage.getContent(), 50))
                                .status(transaction.getParseStatus())
                                .confidence(transaction.getParseConfidence())
                                .messageType("DUPLICATE")
                                .addedToBudget(false)
                                .build());
                        continue;
                    }
                }

                String messageType = detectTransactionPatterns(transaction);

                // ── Transfer detection (replaces manual isSelfTransferCached check) ─────
                if ("TRANSACTION".equals(messageType) && transaction.getAmount() != null) {
                    TransferDetectionResult transferResult = transferDetectionService.detect(
                            transaction, userBankAccounts, Set.of(), dupWindow);
                    if (transferResult.isTransfer()) {
                        messageType = "SELF_TRANSFER";
                        transaction.setCategory("self_transfer");
                        log.debug("Transfer detected for user {}: confidence={}, reason={}",
                                request.getUserId(), transferResult.getConfidenceScore(),
                                transferResult.getReason());
                    }
                }

                transaction.setMessageType(messageType);

                boolean addedToBudget = false;

                if ("TRANSACTION".equals(messageType) && transaction.getAmount() != null
                        && ParseStatus.SUCCESS == transaction.getParseStatus()) {

                    // ── Validation gate ────────────────────────────────────────
                    TransactionValidationResult validation = validatorService.validate(transaction);
                    if (!validation.isValidTransaction()) {
                        log.info("Transaction rejected by validator for user {}: {}",
                                request.getUserId(), validation.getReason());
                        summaries.add(SMSImportResponse.TransactionSummary.builder()
                                .message(truncateMessage(smsMessage.getContent(), 50))
                                .status(transaction.getParseStatus())
                                .confidence(transaction.getParseConfidence())
                                .messageType("INVALID")
                                .addedToBudget(false)
                                .build());
                        continue;
                    }

                    transactionsToSave.add(transaction);

                    // ── Normalize the parsed transaction ──────────────────────────
                    NormalizedTransaction normalized = normalizationService.normalize(
                            transaction, smsMessage.getContent());

                    // ── Flow classification (final authoritative decision) ─────────
                    FlowClassificationResult flow =
                            flowClassifierService.classify(normalized, userBankAccounts);

                    if (flow.getFlowType() == FlowType.INCOME) {
                        String description = normalized.getNormalizedMerchant() != null
                                && !"UNKNOWN".equals(normalized.getNormalizedMerchant())
                                        ? normalized.getNormalizedMerchant()
                                        : (transaction.getMerchant() != null ? transaction.getMerchant()
                                                : "SMS Transaction");
                        Income income = Income.builder()
                                .userId(transaction.getUserId())
                                .amount(transaction.getAmount())
                                .date(transaction.getTransactionDate())
                                .source("SMS_PARSED")
                                .description(description)
                                .build();
                        incomesToSave.add(income);
                        incomeTxIndices.add(transactionsToSave.size() - 1);
                        addedToBudget = true;
                    } else if (flow.getFlowType() == FlowType.EXPENSE) {
                        String description = normalized.getNormalizedMerchant() != null
                                && !"UNKNOWN".equals(normalized.getNormalizedMerchant())
                                        ? normalized.getNormalizedMerchant()
                                        : (transaction.getMerchant() != null ? transaction.getMerchant()
                                                : "SMS Transaction");
                        String normalizedCategory = normalized.getCategory();
                        Expense expense = Expense.builder()
                                .userId(transaction.getUserId())
                                .amount(transaction.getAmount())
                                .expenseDate(transaction.getTransactionDate())
                                .description(description)
                                .notes("SMS Parsed [mode:" + normalized.getMode() + "] - Ref: "
                                        + (normalized.getReferenceId() != null ? normalized.getReferenceId()
                                                : "N/A"))
                                .category(detectCategories(normalizedCategory))
                                .customCategoryName(normalizedCategory)
                                .build();
                        expensesToSave.add(expense);
                        expenseTxIndices.add(transactionsToSave.size() - 1);
                        addedToBudget = true;
                    }

                    if (transaction.getAccountNumber() != null) {
                        this.bankAccountService.addOrUpdateBankAccount(transaction.getUserId(),
                                transaction.getAccountNumber());
                    }
                }

                summaries.add(SMSImportResponse.TransactionSummary.builder()
                        .transactionId(null)
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
        
        if (!transactionsToSave.isEmpty()) {
            List<SMSTransaction> savedTransactions = repository.saveAll(transactionsToSave);

            for (int i = 0; i < Math.min(savedTransactions.size(), summaries.size()); i++) {
                summaries.get(i).setTransactionId(savedTransactions.get(i).getId());
            }

            log.info("Batch saved {} SMS transactions for user {}", savedTransactions.size(), request.getUserId());

            // ── Save income entries with SMS transaction back-link ─────────────────
            if (!incomesToSave.isEmpty()) {
                for (int j = 0; j < incomesToSave.size(); j++) {
                    Income income = incomesToSave.get(j);
                    SMSTransaction savedTx = savedTransactions.get(incomeTxIndices.get(j));
                    income.setSmsTransactionId(savedTx.getId());
                    Income savedIncome = budgetService.addIncome(income);
                    savedTx.setLinkedIncomeId(savedIncome.getId());
                }
                log.info("Created {} income entries from SMS", incomesToSave.size());
            }

            // ── Save expense entries with SMS transaction back-link ────────────────
            if (!expensesToSave.isEmpty()) {
                for (int j = 0; j < expensesToSave.size(); j++) {
                    Expense expense = expensesToSave.get(j);
                    SMSTransaction savedTx = savedTransactions.get(expenseTxIndices.get(j));
                    expense.setSmsTransactionId(savedTx.getId());
                    Expense savedExpense = budgetService.addExpense(expense);
                    savedTx.setLinkedExpenseId(savedExpense.getId());
                }
                log.info("Created {} expense entries from SMS", expensesToSave.size());
            }

            // ── Persist linkedIncomeId / linkedExpenseId back on SMS transactions ──
            if (!incomesToSave.isEmpty() || !expensesToSave.isEmpty()) {
                repository.saveAll(savedTransactions);
            }
        }

        return responseBuilder
                .totalMessages(totalMessages)
                .transactions(summaries)
                .errors(errors)
                .build();
    }
    
    private ExpenseCategory detectCategories(String transactionCategory) {
        return null;
    }

    /**
     * Get user's bank accounts with caching
     * OPTIMIZATION: Cache for 10 minutes to avoid repeated DB queries (80-90% faster)
     * 
     * @param userId The user ID
     * @return Set of user's account numbers
     */
    private Set<String> getUserBankAccounts(Long userId) {
        Set<String> accounts = userBankAccountsCache.get(userId, 
            id -> new HashSet<>(bankAccountRepository.findAccountNumbersByUserId(id)));
        if (log.isDebugEnabled()) {
            log.debug("User {} bank accounts from cache: {}", userId, accounts);
        }
        return accounts;
    }

    /**
     * Legacy method for backward compatibility (for single checks)
     * Check if both accounts belong to the user (proper self-transfer detection)
     *
     * A self-transfer occurs when money moves from one user account to another user account
     *
     * @param userId The user ID
     * @param fromAccount The source account number from SMS (e.g., "XXXX1234")
     * @param toAccount The destination account number from SMS (e.g., "XXXX5678")
     * @return true if BOTH accounts belong to the user, false otherwise
     */
    @Override
    @Transactional
    public SMSTransaction parseSingleMessage(Long userId, String message, String sender) {
        ParsedSMSData parsedData = parserService.parseSMS(message);

        // Convert tags list to comma-separated string
        String tagsStr = parsedData.getTags() != null && !parsedData.getTags().isEmpty()
                ? String.join(",", parsedData.getTags())
                : null;

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
                .category(parsedData.getCategory())
                .tags(tagsStr)
                .isRecurring(parsedData.getIsRecurring() != null ? parsedData.getIsRecurring() : false)
                .dateFromMessage(parsedData.getDateFromMessage())
                .fromAccount(parsedData.getFromAccount())
                .toAccount(parsedData.getToAccount())
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

    @Override
    public List<SMSTransaction> getTransactionsInWindow(Long userId, LocalDate startDate, LocalDate endDate) {
        return repository.findSuccessTransactionsInWindow(userId, startDate, endDate);
    }

    @Override
    public Set<String> getUserBankAccountNumbers(Long userId) {
        return getUserBankAccounts(userId);
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

        String message = transaction.getOriginalMessage();
        String messageLower = message.toLowerCase();

        int score = 0;

        String[] negativeKeywords = {
                "will be", "scheduled", "due", "failed",
                "could not be processed", "declined",
                "not processed", "upcoming", "statement",
                "limit", "disabled", "enabled", "registration",
                "mandate", "auto debit", "autopay"
        };

        if (containsAnyKeyword(messageLower, negativeKeywords)) {
            return "NON_TRANSACTION";
        }

        boolean hasAmount = messageLower.matches(".*(rs\\.?|inr|sgd)\\s?[\\d,]+(\\.\\d+)? .*");
        if (hasAmount)
            score += 3;

        String[] debitKeywords = {
                "debited", "spent", "withdrawn", "paid", "charged", "purchase"
        };

        String[] creditKeywords = {
                "credited", "received", "refund", "cashback", "deposited"
        };

        boolean hasDebit = containsAnyKeywordWithBoundary(messageLower, debitKeywords);
        boolean hasCredit = containsAnyKeywordWithBoundary(messageLower, creditKeywords);

        if (hasDebit)
            score += 3;
        if (hasCredit)
            score += 3;

        if (messageLower.contains("upi") || messageLower.contains("rrn") || messageLower.contains("ref")) {
            score += 2;
        }

        if (messageLower.contains("a/c") || messageLower.contains("account") || messageLower.contains("card")) {
            score += 1;
        }

        String sender = transaction.getSender() != null ? transaction.getSender().toLowerCase() : "";

        if (sender.contains("idfc") || sender.contains("kagbnk") || sender.contains("hdfc")
                || sender.contains("axis")) {
            score += 2;
        }

        String[] mandateKeywords = {
                "will be debited", "will be credited", "scheduled for",
                "auto-debit", "autopay", "next debit", "payment due"
        };

        if (containsAnyKeyword(messageLower, mandateKeywords)) {
            return "MANDATE_ALERT";
        }

        if (hasCredit && hasDebit && hasAmount) {
            return "TRANSACTION";
        }

        if (hasAmount && (hasDebit || hasCredit)) {
            if (score >= 5) {
                return "TRANSACTION";
            } else if (score >= 3) {
                return "LOW_CONFIDENCE_TRANSACTION";
            }
        }

        String[] balanceKeywords = {
                "available balance", "avl bal", "current balance", "balance is"
        };

        if (containsAnyKeyword(messageLower, balanceKeywords)) {
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

}

