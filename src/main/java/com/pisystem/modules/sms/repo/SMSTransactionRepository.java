package com.pisystem.modules.sms.repo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pisystem.modules.sms.data.SMSTransaction;
import com.pisystem.modules.sms.data.SMSTransaction.ParseStatus;
import com.pisystem.modules.sms.data.SMSTransaction.TransactionType;

@Repository
public interface SMSTransactionRepository extends JpaRepository<SMSTransaction, Long> {
    
    List<SMSTransaction> findByUserId(Long userId);
    
    // Batch duplicate check optimization
    @Query("SELECT s.originalMessage FROM SMSTransaction s WHERE s.userId = :userId AND s.originalMessage IN :messages")
    List<String> findExistingMessages(@Param("userId") Long userId, @Param("messages") List<String> messages);
    
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

    /**
     * Fetch all SUCCESS transactions for a user within a tight date window used by
     * the duplicate-detection engine.  Only SUCCESS rows are returned — FAILED and
     * PENDING transactions are deliberately excluded per the detection rules.
     *
     * @param userId    the user whose transactions to search
     * @param startDate window start (inclusive), typically transactionDate - 1 day
     * @param endDate   window end (inclusive), typically transactionDate + 1 day
     */
    @Query("SELECT s FROM SMSTransaction s " +
           "WHERE s.userId = :userId " +
           "  AND s.parseStatus = 'SUCCESS' " +
           "  AND s.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY s.transactionDate ASC, s.transactionTime ASC")
    List<SMSTransaction> findSuccessTransactionsInWindow(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Fast reference-number lookup — used for the STRONG match path.
     * Returns at most one row; multiple rows would indicate a data anomaly.
     */
    @Query("SELECT s FROM SMSTransaction s " +
           "WHERE s.userId = :userId " +
           "  AND s.referenceNumber = :referenceNumber " +
           "  AND s.parseStatus = 'SUCCESS'")
    List<SMSTransaction> findByUserIdAndReferenceNumber(
            @Param("userId") Long userId,
            @Param("referenceNumber") String referenceNumber);

    // ── Raw-text deduplication queries ────────────────────────────────────────

    /**
     * Exact-body lookup restricted to a specific sender.
     * Used by {@code RawSmsDeduplicationServiceImpl} Rule 1 (exact match).
     */
    List<SMSTransaction> findByUserIdAndSenderAndOriginalMessage(
            Long userId, String sender, String originalMessage);

    /**
     * Fetches all messages from a given sender whose {@code createdAt} falls
     * within [{@code from}, {@code to}].
     * Used by {@code RawSmsDeduplicationServiceImpl} Rule 2 (near match).
     *
     * @param userId  the message owner
     * @param sender  SMS sender ID (case-sensitive, as stored)
     * @param from    window start (inclusive)
     * @param to      window end (inclusive)
     */
    @Query("SELECT s FROM SMSTransaction s " +
           "WHERE s.userId = :userId " +
           "  AND s.sender = :sender " +
           "  AND s.createdAt BETWEEN :from AND :to " +
           "ORDER BY s.createdAt ASC")
    List<SMSTransaction> findBySenderInTimeWindow(
            @Param("userId") Long userId,
            @Param("sender") String sender,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
