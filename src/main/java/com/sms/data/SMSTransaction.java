package com.sms.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
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

@Entity
@Table(name = "sms_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Parsed SMS transaction entity with all extracted details")
public class SMSTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "original_message", columnDefinition = "TEXT", nullable = false)
    private String originalMessage;

    @Column(name = "sender")
    private String sender; // SMS sender ID (e.g., bank name)

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "transaction_date")
    private LocalDate transactionDate;

    @Column(name = "transaction_time")
    private LocalTime transactionTime;

    @Column(name = "transaction_type")
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType; // DEBIT or CREDIT

    @Column(name = "merchant")
    private String merchant; // Where the transaction occurred

    @Column(name = "account_number")
    private String accountNumber; // Last 4 digits or masked account number

    @Column(name = "card_number")
    private String cardNumber; // Last 4 digits of card

    @Column(name = "balance")
    private BigDecimal balance; // Available balance after transaction

    @Column(name = "reference_number")
    private String referenceNumber; // Transaction reference or UPI ID

    @Column(name = "upi_id")
    private String upiId; // UPI transaction ID

    @Column(name = "parse_status")
    @Enumerated(EnumType.STRING)
    private ParseStatus parseStatus; // SUCCESS, FAILED, PARTIAL

    @Column(name = "parse_confidence")
    private Double parseConfidence; // 0.0 to 1.0

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "is_processed")
    @Builder.Default
    private Boolean isProcessed = false; // Whether it's been converted to expense/income

    @Column(name = "linked_expense_id")
    private Long linkedExpenseId;

    @Column(name = "linked_income_id")
    private Long linkedIncomeId;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TransactionType {
        DEBIT, CREDIT, UNKNOWN
    }

    public enum ParseStatus {
        SUCCESS, FAILED, PARTIAL
    }
}
