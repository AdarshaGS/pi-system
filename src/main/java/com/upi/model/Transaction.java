package com.upi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "transactions")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId; // Unique transaction reference
    private String senderUpiId;
    private String receiverUpiId;
    private BigDecimal amount;
    private String status; // PENDING, SUCCESS, FAILED, EXPIRED
    @Enumerated(EnumType.STRING)
    private TransactionCategory type; // SEND, RECEIVE, REQUEST, REFUND
    private String remarks;
    private String transactionType; // P2P, P2M, COLLECT_REQUEST, COLLECT_ACCEPT, COLLECT_REJECT
    private String category; // Groceries, Transport, Shopping, etc.
    private String merchantName;
    private String receiptUrl;
    private String errorCode;
    private String errorMessage;
    private Date createdAt;
    private Date completedAt;
}
