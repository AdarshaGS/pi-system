package com.investments.stocks.repo;

import com.investments.stocks.data.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {
    
    List<RecurringTransaction> findByUserId(Long userId);
    
    List<RecurringTransaction> findByUserIdAndStatus(Long userId, RecurringTransaction.Status status);
    
    List<RecurringTransaction> findByUserIdAndType(Long userId, RecurringTransaction.TransactionType type);
    
    @Query("SELECT r FROM RecurringTransaction r WHERE r.status = 'ACTIVE' AND r.autoExecute = true AND r.nextExecutionDate <= :date")
    List<RecurringTransaction> findTransactionsDueForExecution(@Param("date") LocalDate date);
    
    @Query("SELECT r FROM RecurringTransaction r WHERE r.status = 'ACTIVE' AND r.sendReminder = true " +
           "AND r.nextExecutionDate = :date")
    List<RecurringTransaction> findTransactionsForReminder(@Param("date") LocalDate date);
    
    @Query("SELECT r FROM RecurringTransaction r WHERE r.userId = :userId AND r.category = :category AND r.status = 'ACTIVE'")
    List<RecurringTransaction> findActiveTransactionsByCategory(@Param("userId") Long userId, @Param("category") String category);
}
