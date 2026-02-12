package com.upi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "transaction_requests")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String requesterUpiId;
    private String payerUpiId;
    private BigDecimal amount;
    private String status; // PENDING, ACCEPTED, REJECTED, EXPIRED
    private String remarks;
    private Date createdAt;
    private Date respondedAt;
}
