package com.investments.stocks.service;

import com.investments.stocks.data.FinancialGoal;
import com.investments.stocks.data.GoalMilestone;
import com.investments.stocks.dto.FinancialGoalDTO;
import com.investments.stocks.dto.GoalMilestoneDTO;
import com.investments.stocks.exception.ResourceNotFoundException;
import com.investments.stocks.repo.FinancialGoalRepository;
import com.investments.stocks.repo.GoalMilestoneRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FinancialGoalService {
    
    private final FinancialGoalRepository goalRepository;
    private final GoalMilestoneRepository milestoneRepository;
    
    public FinancialGoalService(FinancialGoalRepository goalRepository, 
                               GoalMilestoneRepository milestoneRepository) {
        this.goalRepository = goalRepository;
        this.milestoneRepository = milestoneRepository;
    }
    
    @Transactional
    public FinancialGoalDTO createGoal(FinancialGoalDTO dto) {
        FinancialGoal goal = new FinancialGoal();
        copyDtoToEntity(dto, goal);
        goal = goalRepository.save(goal);
        return convertToDto(goal);
    }
    
    @Transactional
    public FinancialGoalDTO updateGoal(Long id, FinancialGoalDTO dto) {
        FinancialGoal goal = goalRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Financial goal not found with id: " + id));
        
        copyDtoToEntity(dto, goal);
        goal = goalRepository.save(goal);
        return convertToDto(goal);
    }
    
    @Transactional
    public void updateGoalProgress(Long id, BigDecimal currentAmount) {
        FinancialGoal goal = goalRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Financial goal not found with id: " + id));
        
        goal.setCurrentAmount(currentAmount);
        goal.updateProgress();
        goalRepository.save(goal);
    }
    
    public FinancialGoalDTO getGoal(Long id) {
        FinancialGoal goal = goalRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Financial goal not found with id: " + id));
        return convertToDto(goal);
    }
    
    public List<FinancialGoalDTO> getUserGoals(Long userId) {
        return goalRepository.findByUserId(userId).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    public List<FinancialGoalDTO> getActiveGoals(Long userId) {
        return goalRepository.findByUserIdAndStatus(userId, FinancialGoal.GoalStatus.ACTIVE).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    public List<FinancialGoalDTO> getGoalsByType(Long userId, FinancialGoal.GoalType type) {
        return goalRepository.findByUserIdAndGoalType(userId, type).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    public List<FinancialGoalDTO> getGoalsByPriority(Long userId) {
        return goalRepository.findByUserIdOrderedByPriority(userId).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public void deleteGoal(Long id) {
        FinancialGoal goal = goalRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Financial goal not found with id: " + id));
        goalRepository.delete(goal);
    }
    
    @Transactional
    public GoalMilestoneDTO createMilestone(Long goalId, GoalMilestoneDTO dto) {
        // Verify goal exists
        goalRepository.findById(goalId)
            .orElseThrow(() -> new ResourceNotFoundException("Financial goal not found with id: " + goalId));
        
        GoalMilestone milestone = new GoalMilestone();
        milestone.setGoalId(goalId);
        BeanUtils.copyProperties(dto, milestone);
        milestone = milestoneRepository.save(milestone);
        
        GoalMilestoneDTO result = new GoalMilestoneDTO();
        BeanUtils.copyProperties(milestone, result);
        return result;
    }
    
    public List<GoalMilestoneDTO> getGoalMilestones(Long goalId) {
        return milestoneRepository.findByGoalIdOrderByTargetDateAsc(goalId).stream()
            .map(milestone -> {
                GoalMilestoneDTO dto = new GoalMilestoneDTO();
                BeanUtils.copyProperties(milestone, dto);
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    public BigDecimal calculateRequiredMonthlyContribution(Long goalId) {
        FinancialGoal goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new ResourceNotFoundException("Financial goal not found with id: " + goalId));
        
        BigDecimal remaining = goal.getTargetAmount().subtract(goal.getCurrentAmount());
        long monthsRemaining = ChronoUnit.MONTHS.between(LocalDate.now(), goal.getTargetDate());
        
        if (monthsRemaining <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Simple calculation without considering investment returns
        if (goal.getExpectedReturnRate() == null || goal.getExpectedReturnRate().compareTo(BigDecimal.ZERO) == 0) {
            return remaining.divide(BigDecimal.valueOf(monthsRemaining), 2, RoundingMode.HALF_UP);
        }
        
        // Future value of annuity formula considering returns
        BigDecimal monthlyRate = goal.getExpectedReturnRate()
            .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
            .divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_UP);
        
        // FV = PV * (1+r)^n
        BigDecimal futureValueOfCurrent = goal.getCurrentAmount()
            .multiply(BigDecimal.ONE.add(monthlyRate).pow((int) monthsRemaining));
        
        BigDecimal adjustedTarget = goal.getTargetAmount().subtract(futureValueOfCurrent);
        
        // PMT = FV / [((1+r)^n - 1) / r]
        BigDecimal denominator = BigDecimal.ONE.add(monthlyRate).pow((int) monthsRemaining)
            .subtract(BigDecimal.ONE)
            .divide(monthlyRate, 6, RoundingMode.HALF_UP);
        
        return adjustedTarget.divide(denominator, 2, RoundingMode.HALF_UP);
    }
    
    public boolean isGoalOnTrack(Long goalId) {
        FinancialGoal goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new ResourceNotFoundException("Financial goal not found with id: " + goalId));
        
        long totalDays = ChronoUnit.DAYS.between(goal.getStartDate(), goal.getTargetDate());
        long daysPassed = ChronoUnit.DAYS.between(goal.getStartDate(), LocalDate.now());
        
        if (totalDays <= 0) {
            return false;
        }
        
        BigDecimal expectedProgress = BigDecimal.valueOf(daysPassed)
            .divide(BigDecimal.valueOf(totalDays), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
        
        // Consider on track if within 10% of expected progress
        BigDecimal difference = goal.getProgressPercentage().subtract(expectedProgress).abs();
        return difference.compareTo(BigDecimal.valueOf(10)) <= 0;
    }
    
    private FinancialGoalDTO convertToDto(FinancialGoal goal) {
        FinancialGoalDTO dto = new FinancialGoalDTO();
        BeanUtils.copyProperties(goal, dto);
        
        // Calculate additional fields
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), goal.getTargetDate());
        dto.setDaysRemaining(Math.max(0, daysRemaining));
        
        BigDecimal amountRemaining = goal.getTargetAmount().subtract(goal.getCurrentAmount());
        dto.setAmountRemaining(amountRemaining.max(BigDecimal.ZERO));
        
        dto.setRequiredMonthlyContribution(calculateRequiredMonthlyContribution(goal.getId()));
        dto.setOnTrack(isGoalOnTrack(goal.getId()));
        
        // Load milestones
        dto.setMilestones(getGoalMilestones(goal.getId()));
        
        return dto;
    }
    
    private void copyDtoToEntity(FinancialGoalDTO dto, FinancialGoal entity) {
        if (dto.getGoalName() != null) entity.setGoalName(dto.getGoalName());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getGoalType() != null) entity.setGoalType(dto.getGoalType());
        if (dto.getTargetAmount() != null) entity.setTargetAmount(dto.getTargetAmount());
        if (dto.getCurrentAmount() != null) entity.setCurrentAmount(dto.getCurrentAmount());
        if (dto.getTargetDate() != null) entity.setTargetDate(dto.getTargetDate());
        if (dto.getStartDate() != null) entity.setStartDate(dto.getStartDate());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        if (dto.getPriority() != null) entity.setPriority(dto.getPriority());
        if (dto.getExpectedReturnRate() != null) entity.setExpectedReturnRate(dto.getExpectedReturnRate());
        if (dto.getMonthlyContribution() != null) entity.setMonthlyContribution(dto.getMonthlyContribution());
        if (dto.getLinkedAccounts() != null) entity.setLinkedAccounts(dto.getLinkedAccounts());
        if (dto.getAutoContribute() != null) entity.setAutoContribute(dto.getAutoContribute());
        if (dto.getReminderDayOfMonth() != null) entity.setReminderDayOfMonth(dto.getReminderDayOfMonth());
        if (dto.getNotes() != null) entity.setNotes(dto.getNotes());
        if (dto.getUserId() != null) entity.setUserId(dto.getUserId());
    }
}
