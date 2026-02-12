package com.payments.upi;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "upi_transactions")
public class UPITransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String payerVPA;
    private String payeeVPA;
    private double amount;
    private String remarks;
    private String transactionId;
    private String status;
    private LocalDateTime createdAt;

    // Getters and setters
    // ...existing code...
}
