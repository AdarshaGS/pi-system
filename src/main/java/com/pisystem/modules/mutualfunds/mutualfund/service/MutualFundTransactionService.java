package com.mutualfund.service;

import com.mutualfund.model.MutualFundTransaction;
import java.time.LocalDate;
import java.util.List;

public interface MutualFundTransactionService {
    
    MutualFundTransaction addTransaction(Long userId, MutualFundTransaction transaction);
    
    List<MutualFundTransaction> getAllTransactions(Long userId);
    
    List<MutualFundTransaction> getTransactionsByFund(Long userId, Long mutualFundId);
    
    List<MutualFundTransaction> getTransactionsByDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    
    MutualFundTransaction updateTransaction(Long userId, Long transactionId, MutualFundTransaction transaction);
    
    void deleteTransaction(Long userId, Long transactionId);
}
