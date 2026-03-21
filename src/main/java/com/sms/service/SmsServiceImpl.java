package com.sms.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sms.data.ParsedSMSData;
import com.sms.data.SMSImportRequest;
import com.sms.data.SMSImportResponse;
import com.sms.data.SMSTransaction;
import com.sms.repo.SMSTransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsServiceImpl implements SmsService {

    private final SMSParserService parserService;
    private final SMSTransactionRepository repository;

    @Override
    @Transactional
    public SMSImportResponse importMessages(SMSImportRequest request) {
        log.info("Importing {} SMS messages for user {}", request.getMessages().size(), request.getUserId());
        
        SMSImportResponse.SMSImportResponseBuilder responseBuilder = SMSImportResponse.builder();
        List<SMSImportResponse.TransactionSummary> summaries = new ArrayList<>();
        List<SMSImportResponse.ErrorDetail> errors = new ArrayList<>();
        
        int totalMessages = request.getMessages().size();
        int successCount = 0;
        int partialCount = 0;
        int failedCount = 0;
        int duplicateCount = 0;
        
        for (SMSImportRequest.SMSMessage smsMessage : request.getMessages()) {
            try {
                // Check for duplicates
                if (repository.existsByUserIdAndOriginalMessage(request.getUserId(), smsMessage.getContent())) {
                    duplicateCount++;
                    log.debug("Duplicate message skipped");
                    continue;
                }
                
                // Parse and save the message
                SMSTransaction transaction = parseSingleMessage(
                    request.getUserId(), 
                    smsMessage.getContent(), 
                    smsMessage.getSender()
                );
                
                // Count by status
                switch (transaction.getParseStatus()) {
                    case SUCCESS:
                        successCount++;
                        break;
                    case PARTIAL:
                        partialCount++;
                        break;
                    case FAILED:
                        failedCount++;
                        break;
                }
                
                
                // Add to summary
                summaries.add(SMSImportResponse.TransactionSummary.builder()
                    .transactionId(transaction.getId())
                    .message(truncateMessage(smsMessage.getContent(), 50))
                    .status(transaction.getParseStatus())
                    .confidence(transaction.getParseConfidence())
                    .build());
                    
            } catch (Exception e) {
                log.error("Error processing SMS message", e);
                failedCount++;
                errors.add(SMSImportResponse.ErrorDetail.builder()
                    .message(truncateMessage(smsMessage.getContent(), 50))
                    .error(e.getMessage())
                    .build());
            }
        }
        
        return responseBuilder
            .totalMessages(totalMessages)
            .successfullyParsed(successCount)
            .partiallyParsed(partialCount)
            .failed(failedCount)
            .duplicates(duplicateCount)
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
        if (message == null) return "";
        if (message.length() <= maxLength) return message;
        return message.substring(0, maxLength) + "...";
    }
}
