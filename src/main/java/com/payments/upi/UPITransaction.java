package com.payments.upi;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "upi_transactions")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UPITransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // add column names
    @Column(name = "payer_vpa", nullable = false)
    private String payerVPA;

    @Column(name = "payee_vpa", nullable = false)
    private String payeeVPA;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Column(name = "remarks", nullable = false)
    private String remarks;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType; // P2P or P2M

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
