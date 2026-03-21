package com.sms.repo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sms.data.SMSTransaction;
import com.sms.data.SMSTransaction.ParseStatus;
import com.sms.data.SMSTransaction.TransactionType;

@Repository
public interface SMSTransactionRepository extends JpaRepository<SMSTransaction, Long> {
    
    List<SMSTransaction> findByUserId(Long userId);
    
    List<SMSTransaction> findByUserIdAndParseStatus(Long userId, ParseStatus parseStatus);
    
    List<SMSTransaction> findByUserIdAndIsProcessed(Long userId, Boolean isProcessed);
    
    List<SMSTransaction> findByUserIdAndTransactionType(Long userId, TransactionType transactionType);
    
    @Query("SELECT s FROM SMSTransaction s WHERE s.userId = :userId AND s.transactionDate BETWEEN :startDate AND :endDate ORDER BY s.transactionDate DESC, s.transactionTime DESC")
    List<SMSTransaction> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT s FROM SMSTransaction s WHERE s.userId = :userId AND s.parseStatus = 'SUCCESS' AND s.isProcessed = false ORDER BY s.transactionDate DESC")
    List<SMSTransaction> findUnprocessedSuccessfulTransactions(@Param("userId") Long userId);
    
    @Query("SELECT s FROM SMSTransaction s WHERE s.userId = :userId ORDER BY s.createdAt DESC LIMIT :limit")
    List<SMSTransaction> findRecentTransactions(@Param("userId") Long userId, @Param("limit") int limit);
    
    @Query("SELECT COUNT(s) FROM SMSTransaction s WHERE s.userId = :userId AND s.parseStatus = :status")
    Long countByUserIdAndParseStatus(@Param("userId") Long userId, @Param("status") ParseStatus status);
    
    // Check for duplicate messages
    boolean existsByUserIdAndOriginalMessage(Long userId, String originalMessage);
}
