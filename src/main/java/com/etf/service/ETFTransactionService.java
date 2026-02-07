package com.etf.service;

import com.etf.model.ETFTransaction;
import java.time.LocalDate;
import java.util.List;

public interface ETFTransactionService {
    
    ETFTransaction addTransaction(Long userId, ETFTransaction transaction);
    
    List<ETFTransaction> getAllTransactions(Long userId);
    
    List<ETFTransaction> getTransactionsByETF(Long userId, Long etfId);
    
    List<ETFTransaction> getTransactionsByDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    
    ETFTransaction updateTransaction(Long userId, Long transactionId, ETFTransaction transaction);
    
    void deleteTransaction(Long userId, Long transactionId);
}
