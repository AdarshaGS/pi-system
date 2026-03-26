package com.etf.model;

import com.users.data.Users;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "etf_transactions")
public class ETFTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etf_id", nullable = false)
    private ETF etf;
    
    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType; // BUY, SELL
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "price", nullable = false, precision = 15, scale = 4)
    private BigDecimal price;
    
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "brokerage", precision = 10, scale = 2)
    private BigDecimal brokerage = BigDecimal.ZERO;
    
    @Column(name = "stt", precision = 10, scale = 2)
    private BigDecimal stt = BigDecimal.ZERO;
    
    @Column(name = "stamp_duty", precision = 10, scale = 2)
    private BigDecimal stampDuty = BigDecimal.ZERO;
    
    @Column(name = "transaction_charges", precision = 10, scale = 2)
    private BigDecimal transactionCharges = BigDecimal.ZERO;
    
    @Column(name = "gst", precision = 10, scale = 2)
    private BigDecimal gst = BigDecimal.ZERO;
    
    @Column(name = "total_charges", precision = 10, scale = 2)
    private BigDecimal totalCharges = BigDecimal.ZERO;
    
    @Column(name = "net_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal netAmount;
    
    @Column(name = "exchange", nullable = false, length = 10)
    private String exchange;
    
    @Column(name = "order_id", length = 50)
    private String orderId;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
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
    public ETFTransaction() {}
    
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
    
    public String getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
    
    public LocalDate getTransactionDate() {
        return transactionDate;
    }
    
    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public BigDecimal getBrokerage() {
        return brokerage;
    }
    
    public void setBrokerage(BigDecimal brokerage) {
        this.brokerage = brokerage;
    }
    
    public BigDecimal getStt() {
        return stt;
    }
    
    public void setStt(BigDecimal stt) {
        this.stt = stt;
    }
    
    public BigDecimal getStampDuty() {
        return stampDuty;
    }
    
    public void setStampDuty(BigDecimal stampDuty) {
        this.stampDuty = stampDuty;
    }
    
    public BigDecimal getTransactionCharges() {
        return transactionCharges;
    }
    
    public void setTransactionCharges(BigDecimal transactionCharges) {
        this.transactionCharges = transactionCharges;
    }
    
    public BigDecimal getGst() {
        return gst;
    }
    
    public void setGst(BigDecimal gst) {
        this.gst = gst;
    }
    
    public BigDecimal getTotalCharges() {
        return totalCharges;
    }
    
    public void setTotalCharges(BigDecimal totalCharges) {
        this.totalCharges = totalCharges;
    }
    
    public BigDecimal getNetAmount() {
        return netAmount;
    }
    
    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }
    
    public String getExchange() {
        return exchange;
    }
    
    public void setExchange(String exchange) {
        this.exchange = exchange;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
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
