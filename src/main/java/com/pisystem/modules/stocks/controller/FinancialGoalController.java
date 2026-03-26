package com.investments.stocks.controller;

import com.investments.stocks.data.FinancialGoal;
import com.investments.stocks.dto.FinancialGoalDTO;
import com.investments.stocks.dto.GoalMilestoneDTO;
import com.investments.stocks.service.FinancialGoalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/financial-goals")
public class FinancialGoalController {
    
    private final FinancialGoalService goalService;
    
    public FinancialGoalController(FinancialGoalService goalService) {
        this.goalService = goalService;
    }
    
    @PostMapping
    public ResponseEntity<FinancialGoalDTO> createGoal(@RequestBody FinancialGoalDTO goalDTO) {
        FinancialGoalDTO created = goalService.createGoal(goalDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<FinancialGoalDTO> updateGoal(
            @PathVariable Long id,
            @RequestBody FinancialGoalDTO goalDTO) {
        FinancialGoalDTO updated = goalService.updateGoal(id, goalDTO);
        return ResponseEntity.ok(updated);
    }
    
    @PatchMapping("/{id}/progress")
    public ResponseEntity<Void> updateProgress(
            @PathVariable Long id,
            @RequestBody Map<String, BigDecimal> request) {
        BigDecimal currentAmount = request.get("currentAmount");
        goalService.updateGoalProgress(id, currentAmount);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<FinancialGoalDTO> getGoal(@PathVariable Long id) {
        FinancialGoalDTO goal = goalService.getGoal(id);
        return ResponseEntity.ok(goal);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FinancialGoalDTO>> getUserGoals(@PathVariable Long userId) {
        List<FinancialGoalDTO> goals = goalService.getUserGoals(userId);
        return ResponseEntity.ok(goals);
    }
    
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<FinancialGoalDTO>> getActiveGoals(@PathVariable Long userId) {
        List<FinancialGoalDTO> goals = goalService.getActiveGoals(userId);
        return ResponseEntity.ok(goals);
    }
    
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<FinancialGoalDTO>> getGoalsByType(
            @PathVariable Long userId,
            @PathVariable FinancialGoal.GoalType type) {
        List<FinancialGoalDTO> goals = goalService.getGoalsByType(userId, type);
        return ResponseEntity.ok(goals);
    }
    
    @GetMapping("/user/{userId}/priority")
    public ResponseEntity<List<FinancialGoalDTO>> getGoalsByPriority(@PathVariable Long userId) {
        List<FinancialGoalDTO> goals = goalService.getGoalsByPriority(userId);
        return ResponseEntity.ok(goals);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id) {
        goalService.deleteGoal(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{goalId}/milestones")
    public ResponseEntity<GoalMilestoneDTO> createMilestone(
            @PathVariable Long goalId,
            @RequestBody GoalMilestoneDTO milestoneDTO) {
        GoalMilestoneDTO created = goalService.createMilestone(goalId, milestoneDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping("/{goalId}/milestones")
    public ResponseEntity<List<GoalMilestoneDTO>> getGoalMilestones(@PathVariable Long goalId) {
        List<GoalMilestoneDTO> milestones = goalService.getGoalMilestones(goalId);
        return ResponseEntity.ok(milestones);
    }
    
    @GetMapping("/{goalId}/required-contribution")
    public ResponseEntity<Map<String, BigDecimal>> getRequiredContribution(@PathVariable Long goalId) {
        BigDecimal contribution = goalService.calculateRequiredMonthlyContribution(goalId);
        return ResponseEntity.ok(Map.of("requiredMonthlyContribution", contribution));
    }
    
    @GetMapping("/{goalId}/on-track")
    public ResponseEntity<Map<String, Boolean>> isGoalOnTrack(@PathVariable Long goalId) {
        boolean onTrack = goalService.isGoalOnTrack(goalId);
        return ResponseEntity.ok(Map.of("onTrack", onTrack));
    }
}
