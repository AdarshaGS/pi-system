package com.investments.stocks.data;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "recurring_transactions")
public class RecurringTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, length = 3)
    private String currency = "USD";
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Frequency frequency;
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column
    private LocalDate endDate;
    
    @Column
    private LocalDate nextExecutionDate;
    
    @Column
    private LocalDate lastExecutionDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;
    
    @Column(length = 100)
    private String category;
    
    @Column(length = 100)
    private String sourceAccount;
    
    @Column(length = 100)
    private String destinationAccount;
    
    @Column
    private Integer dayOfMonth; // For monthly transactions
    
    @Column
    private Integer dayOfWeek; // For weekly transactions (1-7)
    
    @Column
    private Boolean autoExecute = true;
    
    @Column
    private Boolean sendReminder = false;
    
    @Column
    private Integer reminderDaysBefore = 1;
    
    @Column(nullable = false)
    private Integer executionCount = 0;
    
    @Column
    private Integer maxExecutions;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(length = 500)
    private String notes;
    
    public enum TransactionType {
        INCOME,
        EXPENSE,
        TRANSFER,
        INVESTMENT,
        BILL_PAYMENT,
        LOAN_PAYMENT,
        SAVINGS,
        SUBSCRIPTION
    }
    
    public enum Frequency {
        DAILY,
        WEEKLY,
        BIWEEKLY,
        MONTHLY,
        QUARTERLY,
        SEMI_ANNUAL,
        ANNUAL
    }
    
    public enum Status {
        ACTIVE,
        PAUSED,
        COMPLETED,
        CANCELLED
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (nextExecutionDate == null) {
            calculateNextExecutionDate();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public void calculateNextExecutionDate() {
        LocalDate reference = lastExecutionDate != null ? lastExecutionDate : startDate;
        
        if (reference == null) {
            reference = LocalDate.now();
        }
        
        LocalDate next = switch (frequency) {
            case DAILY -> reference.plusDays(1);
            case WEEKLY -> reference.plusWeeks(1);
            case BIWEEKLY -> reference.plusWeeks(2);
            case MONTHLY -> {
                LocalDate nextMonth = reference.plusMonths(1);
                if (dayOfMonth != null && dayOfMonth > 0 && dayOfMonth <= 31) {
                    int maxDay = nextMonth.lengthOfMonth();
                    int targetDay = Math.min(dayOfMonth, maxDay);
                    yield nextMonth.withDayOfMonth(targetDay);
                }
                yield nextMonth;
            }
            case QUARTERLY -> reference.plusMonths(3);
            case SEMI_ANNUAL -> reference.plusMonths(6);
            case ANNUAL -> reference.plusYears(1);
        };
        
        // If endDate is set and next date exceeds it, mark as completed
        if (endDate != null && next.isAfter(endDate)) {
            status = Status.COMPLETED;
            nextExecutionDate = null;
        } else if (maxExecutions != null && executionCount >= maxExecutions) {
            status = Status.COMPLETED;
            nextExecutionDate = null;
        } else {
            nextExecutionDate = next;
        }
    }
    
    public void markExecuted() {
        lastExecutionDate = LocalDate.now();
        executionCount++;
        calculateNextExecutionDate();
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public TransactionType getType() {
        return type;
    }
    
    public void setType(TransactionType type) {
        this.type = type;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public Frequency getFrequency() {
        return frequency;
    }
    
    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public LocalDate getNextExecutionDate() {
        return nextExecutionDate;
    }
    
    public void setNextExecutionDate(LocalDate nextExecutionDate) {
        this.nextExecutionDate = nextExecutionDate;
    }
    
    public LocalDate getLastExecutionDate() {
        return lastExecutionDate;
    }
    
    public void setLastExecutionDate(LocalDate lastExecutionDate) {
        this.lastExecutionDate = lastExecutionDate;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getSourceAccount() {
        return sourceAccount;
    }
    
    public void setSourceAccount(String sourceAccount) {
        this.sourceAccount = sourceAccount;
    }
    
    public String getDestinationAccount() {
        return destinationAccount;
    }
    
    public void setDestinationAccount(String destinationAccount) {
        this.destinationAccount = destinationAccount;
    }
    
    public Integer getDayOfMonth() {
        return dayOfMonth;
    }
    
    public void setDayOfMonth(Integer dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }
    
    public Integer getDayOfWeek() {
        return dayOfWeek;
    }
    
    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
    
    public Boolean getAutoExecute() {
        return autoExecute;
    }
    
    public void setAutoExecute(Boolean autoExecute) {
        this.autoExecute = autoExecute;
    }
    
    public Boolean getSendReminder() {
        return sendReminder;
    }
    
    public void setSendReminder(Boolean sendReminder) {
        this.sendReminder = sendReminder;
    }
    
    public Integer getReminderDaysBefore() {
        return reminderDaysBefore;
    }
    
    public void setReminderDaysBefore(Integer reminderDaysBefore) {
        this.reminderDaysBefore = reminderDaysBefore;
    }
    
    public Integer getExecutionCount() {
        return executionCount;
    }
    
    public void setExecutionCount(Integer executionCount) {
        this.executionCount = executionCount;
    }
    
    public Integer getMaxExecutions() {
        return maxExecutions;
    }
    
    public void setMaxExecutions(Integer maxExecutions) {
        this.maxExecutions = maxExecutions;
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
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
