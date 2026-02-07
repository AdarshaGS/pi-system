package com.mutualfund.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mutual_funds")
public class MutualFund {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "scheme_code", nullable = false, unique = true, length = 50)
    private String schemeCode;
    
    @Column(name = "scheme_name", nullable = false, length = 500)
    private String schemeName;
    
    @Column(name = "fund_house", nullable = false)
    private String fundHouse;
    
    @Column(name = "scheme_type", length = 100)
    private String schemeType;
    
    @Column(name = "scheme_category", length = 100)
    private String schemeCategory;
    
    @Column(name = "nav", precision = 15, scale = 4)
    private BigDecimal nav;
    
    @Column(name = "nav_date")
    private LocalDate navDate;
    
    @Column(name = "expense_ratio", precision = 5, scale = 2)
    private BigDecimal expenseRatio;
    
    @Column(name = "aum", precision = 20, scale = 2)
    private BigDecimal aum;
    
    @Column(name = "min_investment", precision = 15, scale = 2)
    private BigDecimal minInvestment;
    
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
    
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
    
    // Constructors
    public MutualFund() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSchemeCode() {
        return schemeCode;
    }
    
    public void setSchemeCode(String schemeCode) {
        this.schemeCode = schemeCode;
    }
    
    public String getSchemeName() {
        return schemeName;
    }
    
    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }
    
    public String getFundHouse() {
        return fundHouse;
    }
    
    public void setFundHouse(String fundHouse) {
        this.fundHouse = fundHouse;
    }
    
    public String getSchemeType() {
        return schemeType;
    }
    
    public void setSchemeType(String schemeType) {
        this.schemeType = schemeType;
    }
    
    public String getSchemeCategory() {
        return schemeCategory;
    }
    
    public void setSchemeCategory(String schemeCategory) {
        this.schemeCategory = schemeCategory;
    }
    
    public BigDecimal getNav() {
        return nav;
    }
    
    public void setNav(BigDecimal nav) {
        this.nav = nav;
    }
    
    public LocalDate getNavDate() {
        return navDate;
    }
    
    public void setNavDate(LocalDate navDate) {
        this.navDate = navDate;
    }
    
    public BigDecimal getExpenseRatio() {
        return expenseRatio;
    }
    
    public void setExpenseRatio(BigDecimal expenseRatio) {
        this.expenseRatio = expenseRatio;
    }
    
    public BigDecimal getAum() {
        return aum;
    }
    
    public void setAum(BigDecimal aum) {
        this.aum = aum;
    }
    
    public BigDecimal getMinInvestment() {
        return minInvestment;
    }
    
    public void setMinInvestment(BigDecimal minInvestment) {
        this.minInvestment = minInvestment;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }
    
    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
}
