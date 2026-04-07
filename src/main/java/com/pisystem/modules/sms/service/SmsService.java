package com.pisystem.modules.sms.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.pisystem.modules.sms.data.SMSImportRequest;
import com.pisystem.modules.sms.data.SMSImportResponse;
import com.pisystem.modules.sms.data.SMSTransaction;

public interface SmsService {

    /**
     * Import and parse multiple SMS messages
     * @param request SMS import request containing messages
     * @return Import response with results
     */
    SMSImportResponse importMessages(SMSImportRequest request);
    
    /**
     * Parse and save a single SMS message
     * @param userId User ID
     * @param message SMS message content
     * @param sender SMS sender ID (optional)
     * @return Saved SMS transaction
     */
    SMSTransaction parseSingleMessage(Long userId, String message, String sender);
    
    /**
     * Get all SMS transactions for a user
     * @param userId User ID
     * @return List of SMS transactions
     */
    List<SMSTransaction> getUserTransactions(Long userId);
    
    /**
     * Get unprocessed transactions for a user
     * @param userId User ID
     * @return List of unprocessed transactions
     */
    List<SMSTransaction> getUnprocessedTransactions(Long userId);

    /**
     * Fetch SUCCESS transactions within a date window (for transfer detection).
     */
    List<SMSTransaction> getTransactionsInWindow(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Return the set of account numbers registered by a user (for transfer detection).
     */
    Set<String> getUserBankAccountNumbers(Long userId);

}
