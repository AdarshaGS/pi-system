package com.investments.stocks.dto;

import com.investments.stocks.data.RecurringTransaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RecurringTransactionDTO {
    
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private RecurringTransaction.TransactionType type;
    private BigDecimal amount;
    private String currency;
    private RecurringTransaction.Frequency frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextExecutionDate;
    private LocalDate lastExecutionDate;
    private RecurringTransaction.Status status;
    private String category;
    private String sourceAccount;
    private String destinationAccount;
    private Integer dayOfMonth;
    private Integer dayOfWeek;
    private Boolean autoExecute;
    private Boolean sendReminder;
    private Integer reminderDaysBefore;
    private Integer executionCount;
    private Integer maxExecutions;
    private String notes;
    
    // Calculated fields
    private Long daysUntilNext;
    private BigDecimal totalProcessed;
    private List<RecurringTransactionHistoryDTO> recentHistory;
    
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
    
    public RecurringTransaction.TransactionType getType() {
        return type;
    }
    
    public void setType(RecurringTransaction.TransactionType type) {
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
    
    public RecurringTransaction.Frequency getFrequency() {
        return frequency;
    }
    
    public void setFrequency(RecurringTransaction.Frequency frequency) {
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
    
    public RecurringTransaction.Status getStatus() {
        return status;
    }
    
    public void setStatus(RecurringTransaction.Status status) {
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
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Long getDaysUntilNext() {
        return daysUntilNext;
    }
    
    public void setDaysUntilNext(Long daysUntilNext) {
        this.daysUntilNext = daysUntilNext;
    }
    
    public BigDecimal getTotalProcessed() {
        return totalProcessed;
    }
    
    public void setTotalProcessed(BigDecimal totalProcessed) {
        this.totalProcessed = totalProcessed;
    }
    
    public List<RecurringTransactionHistoryDTO> getRecentHistory() {
        return recentHistory;
    }
    
    public void setRecentHistory(List<RecurringTransactionHistoryDTO> recentHistory) {
        this.recentHistory = recentHistory;
    }
}
