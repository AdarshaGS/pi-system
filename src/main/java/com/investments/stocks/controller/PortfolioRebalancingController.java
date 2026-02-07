package com.investments.stocks.controller;

import com.investments.stocks.service.PortfolioRebalancingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio/rebalance")
public class PortfolioRebalancingController {
    
    private final PortfolioRebalancingService rebalancingService;
    
    public PortfolioRebalancingController(PortfolioRebalancingService rebalancingService) {
        this.rebalancingService = rebalancingService;
    }
    
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzePortfolio(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> currentAllocations = (Map<String, BigDecimal>) request.get("currentAllocations");
        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> targetAllocations = (Map<String, BigDecimal>) request.get("targetAllocations");
        BigDecimal totalValue = new BigDecimal(request.get("totalValue").toString());
        
        Map<String, Object> analysis = rebalancingService.analyzePortfolio(
            currentAllocations, targetAllocations, totalValue
        );
        
        return ResponseEntity.ok(analysis);
    }
    
    @PostMapping("/suggest-allocation")
    public ResponseEntity<Map<String, Object>> suggestOptimalAllocation(@RequestBody Map<String, Object> request) {
        int age = Integer.parseInt(request.get("age").toString());
        String riskTolerance = request.get("riskTolerance").toString();
        int investmentHorizon = Integer.parseInt(request.get("investmentHorizon").toString());
        BigDecimal totalValue = new BigDecimal(request.get("totalValue").toString());
        
        Map<String, Object> allocation = rebalancingService.suggestOptimalAllocation(
            age, riskTolerance, investmentHorizon, totalValue
        );
        
        return ResponseEntity.ok(allocation);
    }
    
    @PostMapping("/tax-efficient")
    public ResponseEntity<Map<String, Object>> calculateTaxEfficientRebalancing(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> holdings = (List<Map<String, Object>>) request.get("holdings");
        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> targetAllocations = (Map<String, BigDecimal>) request.get("targetAllocations");
        BigDecimal totalValue = new BigDecimal(request.get("totalValue").toString());
        
        Map<String, Object> result = rebalancingService.calculateTaxEfficientRebalancing(
            holdings, targetAllocations, totalValue
        );
        
        return ResponseEntity.ok(result);
    }
}
