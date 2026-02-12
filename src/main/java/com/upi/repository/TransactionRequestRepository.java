package com.upi.repository;

import com.upi.model.TransactionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRequestRepository extends JpaRepository<TransactionRequest, Long> {

    List<TransactionRequest> findByPayerUpiIdAndStatus(String payerUpiId, String status);

    List<TransactionRequest> findByRequesterUpiId(String requesterUpiId);

    List<TransactionRequest> findByPayerUpiId(String payerUpiId);
}
