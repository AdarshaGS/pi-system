package com.investments.stocks.dto;

import com.investments.stocks.data.RecurringTransactionHistory;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RecurringTransactionHistoryDTO {
    
    private Long id;
    private Long recurringTransactionId;
    private LocalDateTime executedAt;
    private BigDecimal amount;
    private RecurringTransactionHistory.ExecutionStatus status;
    private String errorMessage;
    private Long transactionId;
    
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
    
    public RecurringTransactionHistory.ExecutionStatus getStatus() {
        return status;
    }
    
    public void setStatus(RecurringTransactionHistory.ExecutionStatus status) {
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
}
