package com.pisystem.modules.upi.repository;

import com.pisystem.modules.upi.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySenderUpiIdOrReceiverUpiId(String senderUpiId, String receiverUpiId);

    List<Transaction> findBySenderUpiIdAndStatus(String senderUpiId, String status);
}
