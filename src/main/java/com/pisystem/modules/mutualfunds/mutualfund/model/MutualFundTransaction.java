package com.mutualfund.model;

import com.users.data.Users;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mutual_fund_transactions")
public class MutualFundTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mutual_fund_id", nullable = false)
    private MutualFund mutualFund;
    
    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType; // BUY, SELL, DIVIDEND_REINVEST, SIP
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;
    
    @Column(name = "units", nullable = false, precision = 15, scale = 4)
    private BigDecimal units;
    
    @Column(name = "nav", nullable = false, precision = 15, scale = 4)
    private BigDecimal nav;
    
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "stamp_duty", precision = 10, scale = 2)
    private BigDecimal stampDuty = BigDecimal.ZERO;
    
    @Column(name = "transaction_charges", precision = 10, scale = 2)
    private BigDecimal transactionCharges = BigDecimal.ZERO;
    
    @Column(name = "stt", precision = 10, scale = 2)
    private BigDecimal stt = BigDecimal.ZERO;
    
    @Column(name = "folio_number", length = 50)
    private String folioNumber;
    
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
    public MutualFundTransaction() {}
    
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
    
    public BigDecimal getUnits() {
        return units;
    }
    
    public void setUnits(BigDecimal units) {
        this.units = units;
    }
    
    public BigDecimal getNav() {
        return nav;
    }
    
    public void setNav(BigDecimal nav) {
        this.nav = nav;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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
    
    public BigDecimal getStt() {
        return stt;
    }
    
    public void setStt(BigDecimal stt) {
        this.stt = stt;
    }
    
    public String getFolioNumber() {
        return folioNumber;
    }
    
    public void setFolioNumber(String folioNumber) {
        this.folioNumber = folioNumber;
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
