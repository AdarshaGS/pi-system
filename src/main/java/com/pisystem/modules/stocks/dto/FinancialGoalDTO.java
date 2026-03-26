package com.investments.stocks.dto;

import com.investments.stocks.data.FinancialGoal;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class FinancialGoalDTO {
    
    private Long id;
    private Long userId;
    private String goalName;
    private String description;
    private FinancialGoal.GoalType goalType;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate targetDate;
    private LocalDate startDate;
    private FinancialGoal.GoalStatus status;
    private FinancialGoal.Priority priority;
    private BigDecimal expectedReturnRate;
    private BigDecimal monthlyContribution;
    private String linkedAccounts;
    private Boolean autoContribute;
    private Integer reminderDayOfMonth;
    private BigDecimal progressPercentage;
    private String notes;
    
    // Calculated fields
    private Long daysRemaining;
    private BigDecimal amountRemaining;
    private BigDecimal requiredMonthlyContribution;
    private Boolean onTrack;
    private List<GoalMilestoneDTO> milestones;
    
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
    
    public String getGoalName() {
        return goalName;
    }
    
    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public FinancialGoal.GoalType getGoalType() {
        return goalType;
    }
    
    public void setGoalType(FinancialGoal.GoalType goalType) {
        this.goalType = goalType;
    }
    
    public BigDecimal getTargetAmount() {
        return targetAmount;
    }
    
    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }
    
    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }
    
    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }
    
    public LocalDate getTargetDate() {
        return targetDate;
    }
    
    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public FinancialGoal.GoalStatus getStatus() {
        return status;
    }
    
    public void setStatus(FinancialGoal.GoalStatus status) {
        this.status = status;
    }
    
    public FinancialGoal.Priority getPriority() {
        return priority;
    }
    
    public void setPriority(FinancialGoal.Priority priority) {
        this.priority = priority;
    }
    
    public BigDecimal getExpectedReturnRate() {
        return expectedReturnRate;
    }
    
    public void setExpectedReturnRate(BigDecimal expectedReturnRate) {
        this.expectedReturnRate = expectedReturnRate;
    }
    
    public BigDecimal getMonthlyContribution() {
        return monthlyContribution;
    }
    
    public void setMonthlyContribution(BigDecimal monthlyContribution) {
        this.monthlyContribution = monthlyContribution;
    }
    
    public String getLinkedAccounts() {
        return linkedAccounts;
    }
    
    public void setLinkedAccounts(String linkedAccounts) {
        this.linkedAccounts = linkedAccounts;
    }
    
    public Boolean getAutoContribute() {
        return autoContribute;
    }
    
    public void setAutoContribute(Boolean autoContribute) {
        this.autoContribute = autoContribute;
    }
    
    public Integer getReminderDayOfMonth() {
        return reminderDayOfMonth;
    }
    
    public void setReminderDayOfMonth(Integer reminderDayOfMonth) {
        this.reminderDayOfMonth = reminderDayOfMonth;
    }
    
    public BigDecimal getProgressPercentage() {
        return progressPercentage;
    }
    
    public void setProgressPercentage(BigDecimal progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Long getDaysRemaining() {
        return daysRemaining;
    }
    
    public void setDaysRemaining(Long daysRemaining) {
        this.daysRemaining = daysRemaining;
    }
    
    public BigDecimal getAmountRemaining() {
        return amountRemaining;
    }
    
    public void setAmountRemaining(BigDecimal amountRemaining) {
        this.amountRemaining = amountRemaining;
    }
    
    public BigDecimal getRequiredMonthlyContribution() {
        return requiredMonthlyContribution;
    }
    
    public void setRequiredMonthlyContribution(BigDecimal requiredMonthlyContribution) {
        this.requiredMonthlyContribution = requiredMonthlyContribution;
    }
    
    public Boolean getOnTrack() {
        return onTrack;
    }
    
    public void setOnTrack(Boolean onTrack) {
        this.onTrack = onTrack;
    }
    
    public List<GoalMilestoneDTO> getMilestones() {
        return milestones;
    }
    
    public void setMilestones(List<GoalMilestoneDTO> milestones) {
        this.milestones = milestones;
    }
}
