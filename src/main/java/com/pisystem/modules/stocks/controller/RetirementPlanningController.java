package com.investments.stocks.controller;

import com.investments.stocks.service.RetirementPlanningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/retirement")
public class RetirementPlanningController {
    
    private final RetirementPlanningService retirementPlanningService;
    
    public RetirementPlanningController(RetirementPlanningService retirementPlanningService) {
        this.retirementPlanningService = retirementPlanningService;
    }
    
    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateRetirementPlan(@RequestBody Map<String, Object> request) {
        int currentAge = Integer.parseInt(request.get("currentAge").toString());
        int retirementAge = Integer.parseInt(request.get("retirementAge").toString());
        BigDecimal currentSavings = new BigDecimal(request.get("currentSavings").toString());
        BigDecimal monthlyContribution = new BigDecimal(request.get("monthlyContribution").toString());
        BigDecimal expectedReturn = new BigDecimal(request.get("expectedReturn").toString());
        BigDecimal inflationRate = new BigDecimal(request.getOrDefault("inflationRate", "3.0").toString());
        BigDecimal desiredMonthlyIncome = new BigDecimal(request.get("desiredMonthlyIncome").toString());
        int lifeExpectancy = Integer.parseInt(request.getOrDefault("lifeExpectancy", "90").toString());
        
        Map<String, Object> plan = retirementPlanningService.calculateRetirementPlan(
            currentAge, retirementAge, currentSavings, monthlyContribution,
            expectedReturn, inflationRate, desiredMonthlyIncome, lifeExpectancy
        );
        
        return ResponseEntity.ok(plan);
    }
    
    @PostMapping("/withdrawal-rate")
    public ResponseEntity<Map<String, Object>> calculateWithdrawalRate(@RequestBody Map<String, Object> request) {
        BigDecimal portfolioValue = new BigDecimal(request.get("portfolioValue").toString());
        int withdrawalYears = Integer.parseInt(request.getOrDefault("withdrawalYears", "30").toString());
        
        Map<String, Object> result = retirementPlanningService.calculateSafWithdrawalRate(portfolioValue, withdrawalYears);
        return ResponseEntity.ok(result);
    }
}
