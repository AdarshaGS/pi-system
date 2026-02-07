package com.investments.stocks.data;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cash_flow_records")
public class CashFlowRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private LocalDate recordDate;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalIncome = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalExpenses = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal netCashFlow = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal salaryIncome = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal investmentIncome = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal businessIncome = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal otherIncome = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal housingExpenses = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal transportationExpenses = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal foodExpenses = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal utilitiesExpenses = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal entertainmentExpenses = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal healthcareExpenses = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal educationExpenses = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal debtPayments = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal savingsContributions = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal investmentContributions = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal otherExpenses = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PeriodType periodType;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum PeriodType {
        DAILY,
        WEEKLY,
        MONTHLY,
        QUARTERLY,
        ANNUAL
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateNetCashFlow();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateNetCashFlow();
    }
    
    public void calculateNetCashFlow() {
        netCashFlow = totalIncome.subtract(totalExpenses);
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public LocalDate getRecordDate() {
        return recordDate;
    }
    
    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }
    
    public BigDecimal getTotalIncome() {
        return totalIncome;
    }
    
    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }
    
    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }
    
    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }
    
    public BigDecimal getNetCashFlow() {
        return netCashFlow;
    }
    
    public void setNetCashFlow(BigDecimal netCashFlow) {
        this.netCashFlow = netCashFlow;
    }
    
    public BigDecimal getSalaryIncome() {
        return salaryIncome;
    }
    
    public void setSalaryIncome(BigDecimal salaryIncome) {
        this.salaryIncome = salaryIncome;
    }
    
    public BigDecimal getInvestmentIncome() {
        return investmentIncome;
    }
    
    public void setInvestmentIncome(BigDecimal investmentIncome) {
        this.investmentIncome = investmentIncome;
    }
    
    public BigDecimal getBusinessIncome() {
        return businessIncome;
    }
    
    public void setBusinessIncome(BigDecimal businessIncome) {
        this.businessIncome = businessIncome;
    }
    
    public BigDecimal getOtherIncome() {
        return otherIncome;
    }
    
    public void setOtherIncome(BigDecimal otherIncome) {
        this.otherIncome = otherIncome;
    }
    
    public BigDecimal getHousingExpenses() {
        return housingExpenses;
    }
    
    public void setHousingExpenses(BigDecimal housingExpenses) {
        this.housingExpenses = housingExpenses;
    }
    
    public BigDecimal getTransportationExpenses() {
        return transportationExpenses;
    }
    
    public void setTransportationExpenses(BigDecimal transportationExpenses) {
        this.transportationExpenses = transportationExpenses;
    }
    
    public BigDecimal getFoodExpenses() {
        return foodExpenses;
    }
    
    public void setFoodExpenses(BigDecimal foodExpenses) {
        this.foodExpenses = foodExpenses;
    }
    
    public BigDecimal getUtilitiesExpenses() {
        return utilitiesExpenses;
    }
    
    public void setUtilitiesExpenses(BigDecimal utilitiesExpenses) {
        this.utilitiesExpenses = utilitiesExpenses;
    }
    
    public BigDecimal getEntertainmentExpenses() {
        return entertainmentExpenses;
    }
    
    public void setEntertainmentExpenses(BigDecimal entertainmentExpenses) {
        this.entertainmentExpenses = entertainmentExpenses;
    }
    
    public BigDecimal getHealthcareExpenses() {
        return healthcareExpenses;
    }
    
    public void setHealthcareExpenses(BigDecimal healthcareExpenses) {
        this.healthcareExpenses = healthcareExpenses;
    }
    
    public BigDecimal getEducationExpenses() {
        return educationExpenses;
    }
    
    public void setEducationExpenses(BigDecimal educationExpenses) {
        this.educationExpenses = educationExpenses;
    }
    
    public BigDecimal getDebtPayments() {
        return debtPayments;
    }
    
    public void setDebtPayments(BigDecimal debtPayments) {
        this.debtPayments = debtPayments;
    }
    
    public BigDecimal getSavingsContributions() {
        return savingsContributions;
    }
    
    public void setSavingsContributions(BigDecimal savingsContributions) {
        this.savingsContributions = savingsContributions;
    }
    
    public BigDecimal getInvestmentContributions() {
        return investmentContributions;
    }
    
    public void setInvestmentContributions(BigDecimal investmentContributions) {
        this.investmentContributions = investmentContributions;
    }
    
    public BigDecimal getOtherExpenses() {
        return otherExpenses;
    }
    
    public void setOtherExpenses(BigDecimal otherExpenses) {
        this.otherExpenses = otherExpenses;
    }
    
    public PeriodType getPeriodType() {
        return periodType;
    }
    
    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
