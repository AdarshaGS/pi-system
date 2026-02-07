package com.etf.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "etfs")
public class ETF {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "symbol", nullable = false, unique = true, length = 20)
    private String symbol;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "isin", unique = true, length = 20)
    private String isin;
    
    @Column(name = "exchange", nullable = false, length = 10)
    private String exchange; // NSE, BSE
    
    @Column(name = "etf_type", nullable = false, length = 50)
    private String etfType; // INDEX, GOLD, SILVER, INTERNATIONAL, SECTORAL
    
    @Column(name = "underlying_index", length = 100)
    private String underlyingIndex;
    
    @Column(name = "fund_house", nullable = false)
    private String fundHouse;
    
    @Column(name = "expense_ratio", precision = 5, scale = 2)
    private BigDecimal expenseRatio;
    
    @Column(name = "aum", precision = 20, scale = 2)
    private BigDecimal aum;
    
    @Column(name = "nav", precision = 15, scale = 4)
    private BigDecimal nav;
    
    @Column(name = "market_price", precision = 15, scale = 4)
    private BigDecimal marketPrice;
    
    @Column(name = "price_date")
    private LocalDate priceDate;
    
    @Column(name = "tracking_error", precision = 5, scale = 2)
    private BigDecimal trackingError;
    
    @Column(name = "dividend_yield", precision = 5, scale = 2)
    private BigDecimal dividendYield;
    
    @Column(name = "lot_size")
    private Integer lotSize = 1;
    
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
    public ETF() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getIsin() {
        return isin;
    }
    
    public void setIsin(String isin) {
        this.isin = isin;
    }
    
    public String getExchange() {
        return exchange;
    }
    
    public void setExchange(String exchange) {
        this.exchange = exchange;
    }
    
    public String getEtfType() {
        return etfType;
    }
    
    public void setEtfType(String etfType) {
        this.etfType = etfType;
    }
    
    public String getUnderlyingIndex() {
        return underlyingIndex;
    }
    
    public void setUnderlyingIndex(String underlyingIndex) {
        this.underlyingIndex = underlyingIndex;
    }
    
    public String getFundHouse() {
        return fundHouse;
    }
    
    public void setFundHouse(String fundHouse) {
        this.fundHouse = fundHouse;
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
    
    public BigDecimal getNav() {
        return nav;
    }
    
    public void setNav(BigDecimal nav) {
        this.nav = nav;
    }
    
    public BigDecimal getMarketPrice() {
        return marketPrice;
    }
    
    public void setMarketPrice(BigDecimal marketPrice) {
        this.marketPrice = marketPrice;
    }
    
    public LocalDate getPriceDate() {
        return priceDate;
    }
    
    public void setPriceDate(LocalDate priceDate) {
        this.priceDate = priceDate;
    }
    
    public BigDecimal getTrackingError() {
        return trackingError;
    }
    
    public void setTrackingError(BigDecimal trackingError) {
        this.trackingError = trackingError;
    }
    
    public BigDecimal getDividendYield() {
        return dividendYield;
    }
    
    public void setDividendYield(BigDecimal dividendYield) {
        this.dividendYield = dividendYield;
    }
    
    public Integer getLotSize() {
        return lotSize;
    }
    
    public void setLotSize(Integer lotSize) {
        this.lotSize = lotSize;
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
