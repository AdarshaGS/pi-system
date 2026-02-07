package com.etf.model;

import com.users.data.Users;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "etf_holdings", uniqueConstraints = {
        @UniqueConstraint(name = "unique_s_etf", columnNames = { "user_id", "etf_id" })
})
public class ETFHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etf_id", nullable = false)
    private ETF etf;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity = 0;

    @Column(name = "average_price", nullable = false, precision = 15, scale = 4)
    private BigDecimal averagePrice = BigDecimal.ZERO;

    @Column(name = "invested_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal investedAmount = BigDecimal.ZERO;

    @Column(name = "current_price", precision = 15, scale = 4)
    private BigDecimal currentPrice;

    @Column(name = "current_value", precision = 15, scale = 2)
    private BigDecimal currentValue;

    @Column(name = "unrealized_gain", precision = 15, scale = 2)
    private BigDecimal unrealizedGain;

    @Column(name = "unrealized_gain_percentage", precision = 10, scale = 2)
    private BigDecimal unrealizedGainPercentage;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    // Constructors
    public ETFHolding() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public ETF getEtf() {
        return etf;
    }

    public void setEtf(ETF etf) {
        this.etf = etf;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }

    public BigDecimal getInvestedAmount() {
        return investedAmount;
    }

    public void setInvestedAmount(BigDecimal investedAmount) {
        this.investedAmount = investedAmount;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }

    public BigDecimal getUnrealizedGain() {
        return unrealizedGain;
    }

    public void setUnrealizedGain(BigDecimal unrealizedGain) {
        this.unrealizedGain = unrealizedGain;
    }

    public BigDecimal getUnrealizedGainPercentage() {
        return unrealizedGainPercentage;
    }

    public void setUnrealizedGainPercentage(BigDecimal unrealizedGainPercentage) {
        this.unrealizedGainPercentage = unrealizedGainPercentage;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
