package com.investments.stocks.controller;

import com.investments.stocks.service.CashFlowAnalysisService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/cash-flow")
public class CashFlowController {
    
    private final CashFlowAnalysisService cashFlowAnalysisService;
    
    public CashFlowController(CashFlowAnalysisService cashFlowAnalysisService) {
        this.cashFlowAnalysisService = cashFlowAnalysisService;
    }
    
    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<Map<String, Object>> getCashFlowSummary(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> summary = cashFlowAnalysisService.getCashFlowSummary(userId, startDate, endDate);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/user/{userId}/projections")
    public ResponseEntity<Map<String, Object>> getProjections(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "12") int months) {
        Map<String, Object> projections = cashFlowAnalysisService.getProjections(userId, months);
        return ResponseEntity.ok(projections);
    }
    
    @GetMapping("/user/{userId}/category-breakdown")
    public ResponseEntity<Map<String, BigDecimal>> getCategoryBreakdown(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, BigDecimal> breakdown = cashFlowAnalysisService.getCategoryBreakdown(userId, startDate, endDate);
        return ResponseEntity.ok(breakdown);
    }
}
