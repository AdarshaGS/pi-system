package com.etf.repository;

import com.etf.model.ETFTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ETFTransactionRepository extends JpaRepository<ETFTransaction, Long> {
    
    List<ETFTransaction> findByUserId(Long userId);
    
    List<ETFTransaction> findByUserIdAndEtfId(Long userId, Long etfId);
    
    List<ETFTransaction> findByUserIdAndTransactionDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    List<ETFTransaction> findByUserIdAndTransactionType(Long userId, String transactionType);
}
