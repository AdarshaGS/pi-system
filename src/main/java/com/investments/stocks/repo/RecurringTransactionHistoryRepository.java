package com.investments.stocks.repo;

import com.investments.stocks.data.RecurringTransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RecurringTransactionHistoryRepository extends JpaRepository<RecurringTransactionHistory, Long> {
    
    List<RecurringTransactionHistory> findByRecurringTransactionId(Long recurringTransactionId);
    
    List<RecurringTransactionHistory> findByRecurringTransactionIdAndStatus(
        Long recurringTransactionId, 
        RecurringTransactionHistory.ExecutionStatus status
    );
    
    @Query("SELECT h FROM RecurringTransactionHistory h WHERE h.recurringTransactionId = :id " +
           "AND h.executedAt BETWEEN :startDate AND :endDate ORDER BY h.executedAt DESC")
    List<RecurringTransactionHistory> findHistoryByDateRange(
        @Param("id") Long recurringTransactionId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
