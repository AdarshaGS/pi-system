package com.investments.stocks.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for transaction statistics
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Transaction statistics for a user")
public class TransactionStats {

    @Schema(description = "Total number of transactions", example = "25")
    private Long totalTransactions;

    @Schema(description = "Number of BUY transactions", example = "15")
    private Long buyCount;

    @Schema(description = "Number of SELL transactions", example = "10")
    private Long sellCount;

    @Schema(description = "Total amount invested (all BUY transactions)", example = "125000.00")
    private BigDecimal totalInvested;

    @Schema(description = "Total realized gains from SELL transactions", example = "15000.00")
    private BigDecimal totalRealizedGains;

    @Schema(description = "Average transaction size", example = "5000.00")
    private BigDecimal averageTransactionSize;
}
