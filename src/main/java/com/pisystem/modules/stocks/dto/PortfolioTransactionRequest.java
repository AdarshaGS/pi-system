package com.investments.stocks.dto;

import com.investments.stocks.data.PortfolioTransaction.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating/updating portfolio transactions
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Portfolio transaction request")
public class PortfolioTransactionRequest {

    @NotNull(message = "User ID is required")
    @Schema(description = "User ID", example = "123", required = true)
    private Long userId;

    @NotBlank(message = "Symbol is required")
    @Size(min = 1, max = 50, message = "Symbol must be between 1 and 50 characters")
    @Schema(description = "Stock symbol", example = "RELIANCE", required = true)
    private String symbol;

    @NotNull(message = "Transaction type is required")
    @Schema(description = "Type of transaction", example = "BUY", required = true)
    private TransactionType transactionType;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Number of shares", example = "10", required = true)
    private Integer quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Schema(description = "Price per share", example = "2450.50", required = true)
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Fees cannot be negative")
    @Schema(description = "Brokerage and other fees", example = "25.00")
    private BigDecimal fees;

    @Schema(description = "Transaction date", example = "2024-01-15")
    private LocalDate transactionDate;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Schema(description = "Additional notes", example = "Bought at support level")
    private String notes;
}
