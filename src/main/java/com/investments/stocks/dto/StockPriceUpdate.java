package com.investments.stocks.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for real-time stock price updates sent via WebSocket.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockPriceUpdate {
    
    private String symbol;
    private BigDecimal currentPrice;
    private BigDecimal previousClose;
    private BigDecimal change;
    private BigDecimal changePercent;
    private Long volume;
    private BigDecimal dayHigh;
    private BigDecimal dayLow;
    private LocalDateTime timestamp;
    
    /**
     * Calculate change and change percentage.
     */
    public void calculateChange() {
        if (currentPrice != null && previousClose != null && previousClose.compareTo(BigDecimal.ZERO) > 0) {
            this.change = currentPrice.subtract(previousClose);
            this.changePercent = change.divide(previousClose, 4, BigDecimal.ROUND_HALF_UP)
                                      .multiply(BigDecimal.valueOf(100));
        }
    }
}
