package com.investments.stocks.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing portfolio transactions (Buy, Sell, Dividend, etc.)
 * Tracks all stock trading activity for users
 */
@Entity
@Table(name = "portfolio_transactions", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_symbol", columnList = "symbol"),
    @Index(name = "idx_transaction_date", columnList = "transaction_date"),
    @Index(name = "idx_user_symbol", columnList = "user_id, symbol")
})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Portfolio transaction record")
public class PortfolioTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique transaction ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Schema(description = "User ID who made the transaction", example = "123", required = true)
    private Long userId;

    @Column(name = "symbol", nullable = false, length = 50)
    @Schema(description = "Stock symbol (e.g., RELIANCE, TCS)", example = "RELIANCE", required = true)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    @Schema(description = "Type of transaction", example = "BUY", required = true)
    private TransactionType transactionType;

    @Column(name = "quantity", nullable = false)
    @Schema(description = "Number of shares", example = "10", required = true)
    private Integer quantity;

    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    @Schema(description = "Price per share at the time of transaction", example = "2450.50", required = true)
    private BigDecimal price;

    @Column(name = "fees", precision = 15, scale = 2)
    @Schema(description = "Brokerage and other fees", example = "25.00")
    @Builder.Default
    private BigDecimal fees = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    @Schema(description = "Total amount (quantity * price + fees for BUY, quantity * price - fees for SELL)", 
            example = "24530.00", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal totalAmount;

    @Column(name = "transaction_date", nullable = false)
    @Schema(description = "Date when the transaction occurred", example = "2024-01-15", required = true)
    private LocalDate transactionDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    @Schema(description = "Additional notes about the transaction", example = "Bought at support level")
    private String notes;

    @Column(name = "realized_gain", precision = 15, scale = 2)
    @Schema(description = "Realized profit/loss for SELL transactions", example = "1500.00", accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal realizedGain;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Record creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Schema(description = "Record last update timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    /**
     * Transaction type enum
     */
    public enum TransactionType {
        BUY,        // Purchase of stocks
        SELL,       // Sale of stocks
        DIVIDEND,   // Dividend received
        BONUS,      // Bonus shares received
        SPLIT,      // Stock split adjustment
        MERGER      // Merger/acquisition adjustment
    }

    /**
     * Calculate total amount before saving
     */
    @PrePersist
    @PreUpdate
    public void calculateTotalAmount() {
        if (quantity != null && price != null) {
            BigDecimal baseAmount = price.multiply(BigDecimal.valueOf(quantity));
            BigDecimal transactionFees = fees != null ? fees : BigDecimal.ZERO;
            
            if (transactionType == TransactionType.BUY) {
                totalAmount = baseAmount.add(transactionFees);
            } else if (transactionType == TransactionType.SELL) {
                totalAmount = baseAmount.subtract(transactionFees);
            } else {
                totalAmount = baseAmount;
            }
        }
        
        if (updatedAt == null && createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
}
