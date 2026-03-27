package com.pisystem.modules.upi.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pisystem.modules.upi.model.BankAccount;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    // Spring Data JPA derived query - uses nested property path
    Optional<BankAccount> findByUser_IdAndAccountNumber(Long userId, String accountNumber);

    // Spring Data JPA derived query - uses nested property path
    Collection<BankAccount> findByUser_Id(Long userId);
    
    // Optimized query to fetch only account numbers (for caching)
    @Query("SELECT b.accountNumber FROM BankAccount b WHERE b.user.id = :userId")
    List<String> findAccountNumbersByUserId(@Param("userId") Long userId);
}
