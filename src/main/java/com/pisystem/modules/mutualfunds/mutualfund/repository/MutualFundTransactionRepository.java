package com.mutualfund.repository;

import com.mutualfund.model.MutualFundTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MutualFundTransactionRepository extends JpaRepository<MutualFundTransaction, Long> {
    
    List<MutualFundTransaction> findByUserId(Long userId);
    
    List<MutualFundTransaction> findByUserIdAndMutualFundId(Long userId, Long mutualFundId);
    
    List<MutualFundTransaction> findByUserIdAndTransactionDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    List<MutualFundTransaction> findByUserIdAndTransactionType(Long userId, String transactionType);
}
