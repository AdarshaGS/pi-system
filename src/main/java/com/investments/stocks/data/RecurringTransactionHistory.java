package com.investments.stocks.data;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "recurring_transaction_history")
public class RecurringTransactionHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long recurringTransactionId;
    
    @Column(nullable = false)
    private LocalDateTime executedAt;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExecutionStatus status;
    
    @Column(length = 500)
    private String errorMessage;
    
    @Column
    private Long transactionId; // Reference to actual transaction created
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    public enum ExecutionStatus {
        SUCCESS,
        FAILED,
        SKIPPED,
        PENDING
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getRecurringTransactionId() {
        return recurringTransactionId;
    }
    
    public void setRecurringTransactionId(Long recurringTransactionId) {
        this.recurringTransactionId = recurringTransactionId;
    }
    
    public LocalDateTime getExecutedAt() {
        return executedAt;
    }
    
    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public ExecutionStatus getStatus() {
        return status;
    }
    
    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Long getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
