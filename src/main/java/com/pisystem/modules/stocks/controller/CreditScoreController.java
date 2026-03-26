package com.investments.stocks.controller;

import com.investments.stocks.data.CreditScore;
import com.investments.stocks.service.CreditScoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/credit-score")
public class CreditScoreController {
    
    private final CreditScoreService creditScoreService;
    
    public CreditScoreController(CreditScoreService creditScoreService) {
        this.creditScoreService = creditScoreService;
    }
    
    @PostMapping
    public ResponseEntity<CreditScore> recordCreditScore(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        Integer score = Integer.valueOf(request.get("score").toString());
        String provider = request.get("provider").toString();
        String factors = request.getOrDefault("factors", "").toString();
        String recommendations = request.getOrDefault("recommendations", "").toString();
        
        CreditScore creditScore = creditScoreService.recordCreditScore(userId, score, provider, factors, recommendations);
        return ResponseEntity.status(HttpStatus.CREATED).body(creditScore);
    }
    
    @GetMapping("/user/{userId}/latest")
    public ResponseEntity<CreditScore> getLatestScore(@PathVariable Long userId) {
        CreditScore score = creditScoreService.getLatestScore(userId);
        return score != null ? ResponseEntity.ok(score) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/user/{userId}/history")
    public ResponseEntity<List<CreditScore>> getCreditScoreHistory(@PathVariable Long userId) {
        List<CreditScore> history = creditScoreService.getCreditScoreHistory(userId);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/user/{userId}/analysis")
    public ResponseEntity<Map<String, Object>> getCreditScoreAnalysis(@PathVariable Long userId) {
        Map<String, Object> analysis = creditScoreService.getCreditScoreAnalysis(userId);
        return ResponseEntity.ok(analysis);
    }
}
