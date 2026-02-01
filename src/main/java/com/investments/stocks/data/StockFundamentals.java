package com.investments.stocks.data;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_fundamentals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockFundamentals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "symbol", nullable = false, unique = true, length = 50)
    private String symbol;

    @Column(name = "market_cap", precision = 20, scale = 2)
    private BigDecimal marketCap;

    @Column(name = "pe_ratio", precision = 10, scale = 2)
    private BigDecimal peRatio;

    @Column(name = "pb_ratio", precision = 10, scale = 2)
    private BigDecimal pbRatio;

    @Column(name = "dividend_yield", precision = 5, scale = 2)
    private BigDecimal dividendYield;

    @Column(name = "eps", precision = 10, scale = 2)
    private BigDecimal eps;

    @Column(name = "roe", precision = 10, scale = 2)
    private BigDecimal roe;

    @Column(name = "roa", precision = 10, scale = 2)
    private BigDecimal roa;

    @Column(name = "week_52_high", precision = 15, scale = 2)
    private BigDecimal week52High;

    @Column(name = "week_52_low", precision = 15, scale = 2)
    private BigDecimal week52Low;

    @Column(name = "book_value", precision = 15, scale = 2)
    private BigDecimal bookValue;

    @Column(name = "face_value", precision = 10, scale = 2)
    private BigDecimal faceValue;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
