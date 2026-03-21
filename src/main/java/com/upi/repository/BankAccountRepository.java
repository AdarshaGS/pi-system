package com.upi.repository;

import com.upi.model.BankAccount;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    Optional<BankAccount> findByUserIdAndAccountNumber(Long userId, String accountNumber);
}
