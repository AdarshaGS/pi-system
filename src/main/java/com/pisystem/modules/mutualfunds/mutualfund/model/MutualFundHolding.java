package com.mutualfund.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.users.data.Users;

@Entity
@Table(name = "mutual_fund_holdings", uniqueConstraints = {
    @UniqueConstraint(name = "unique_user_fund_folio", 
                      columnNames = {"user_id", "mutual_fund_id", "folio_number"})
})
public class MutualFundHolding {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mutual_fund_id", nullable = false)
    private MutualFund mutualFund;
    
    @Column(name = "folio_number", length = 50)
    private String folioNumber;
    
    @Column(name = "total_units", nullable = false, precision = 15, scale = 4)
    private BigDecimal totalUnits = BigDecimal.ZERO;
    
    @Column(name = "average_nav", nullable = false, precision = 15, scale = 4)
    private BigDecimal averageNav = BigDecimal.ZERO;
    
    @Column(name = "invested_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal investedAmount = BigDecimal.ZERO;
    
    @Column(name = "current_nav", precision = 15, scale = 4)
    private BigDecimal currentNav;
    
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
    public MutualFundHolding() {}
    
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
    
    public MutualFund getMutualFund() {
        return mutualFund;
    }
    
    public void setMutualFund(MutualFund mutualFund) {
        this.mutualFund = mutualFund;
    }
    
    public String getFolioNumber() {
        return folioNumber;
    }
    
    public void setFolioNumber(String folioNumber) {
        this.folioNumber = folioNumber;
    }
    
    public BigDecimal getTotalUnits() {
        return totalUnits;
    }
    
    public void setTotalUnits(BigDecimal totalUnits) {
        this.totalUnits = totalUnits;
    }
    
    public BigDecimal getAverageNav() {
        return averageNav;
    }
    
    public void setAverageNav(BigDecimal averageNav) {
        this.averageNav = averageNav;
    }
    
    public BigDecimal getInvestedAmount() {
        return investedAmount;
    }
    
    public void setInvestedAmount(BigDecimal investedAmount) {
        this.investedAmount = investedAmount;
    }
    
    public BigDecimal getCurrentNav() {
        return currentNav;
    }
    
    public void setCurrentNav(BigDecimal currentNav) {
        this.currentNav = currentNav;
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
