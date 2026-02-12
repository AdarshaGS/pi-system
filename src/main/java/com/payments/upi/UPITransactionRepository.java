package com.payments.upi;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UPITransactionRepository extends JpaRepository<UPITransaction, Long> {
    UPITransaction findByTransactionId(String transactionId);
}
