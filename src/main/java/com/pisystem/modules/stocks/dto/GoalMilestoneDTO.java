package com.investments.stocks.dto;

import com.investments.stocks.data.GoalMilestone;
import java.math.BigDecimal;
import java.time.LocalDate;

public class GoalMilestoneDTO {
    
    private Long id;
    private Long goalId;
    private String milestoneName;
    private String description;
    private BigDecimal targetAmount;
    private LocalDate targetDate;
    private LocalDate achievedDate;
    private GoalMilestone.MilestoneStatus status;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getGoalId() {
        return goalId;
    }
    
    public void setGoalId(Long goalId) {
        this.goalId = goalId;
    }
    
    public String getMilestoneName() {
        return milestoneName;
    }
    
    public void setMilestoneName(String milestoneName) {
        this.milestoneName = milestoneName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getTargetAmount() {
        return targetAmount;
    }
    
    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }
    
    public LocalDate getTargetDate() {
        return targetDate;
    }
    
    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }
    
    public LocalDate getAchievedDate() {
        return achievedDate;
    }
    
    public void setAchievedDate(LocalDate achievedDate) {
        this.achievedDate = achievedDate;
    }
    
    public GoalMilestone.MilestoneStatus getStatus() {
        return status;
    }
    
    public void setStatus(GoalMilestone.MilestoneStatus status) {
        this.status = status;
    }
}
