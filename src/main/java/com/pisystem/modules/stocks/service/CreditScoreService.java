package com.investments.stocks.service;

import com.investments.stocks.data.CreditScore;
import com.investments.stocks.repo.CreditScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CreditScoreService {
    
    private final CreditScoreRepository creditScoreRepository;
    
    public CreditScoreService(CreditScoreRepository creditScoreRepository) {
        this.creditScoreRepository = creditScoreRepository;
    }
    
    @Transactional
    public CreditScore recordCreditScore(Long userId, Integer score, String provider, 
                                        String factors, String recommendations) {
        // Get previous score to calculate change
        Optional<CreditScore> previousScore = creditScoreRepository.findLatestScoreByProvider(userId, provider);
        
        CreditScore creditScore = new CreditScore();
        creditScore.setUserId(userId);
        creditScore.setScore(score);
        creditScore.setProvider(provider);
        creditScore.setRecordDate(LocalDateTime.now());
        creditScore.setFactors(factors);
        creditScore.setRecommendations(recommendations);
        
        if (previousScore.isPresent()) {
            creditScore.setChangeFromPrevious(score - previousScore.get().getScore());
        }
        
        return creditScoreRepository.save(creditScore);
    }
    
    public CreditScore getLatestScore(Long userId) {
        return creditScoreRepository.findLatestScore(userId).orElse(null);
    }
    
    public List<CreditScore> getCreditScoreHistory(Long userId) {
        return creditScoreRepository.findByUserIdOrderByRecordDateDesc(userId);
    }
    
    public Map<String, Object> getCreditScoreAnalysis(Long userId) {
        List<CreditScore> history = creditScoreRepository.findByUserIdOrderByRecordDateDesc(userId);
        
        if (history.isEmpty()) {
            return Collections.emptyMap();
        }
        
        CreditScore latest = history.get(0);
        
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("currentScore", latest.getScore());
        analysis.put("rating", latest.getScoreRating());
        analysis.put("provider", latest.getProvider());
        analysis.put("lastUpdated", latest.getRecordDate());
        analysis.put("changeFromPrevious", latest.getChangeFromPrevious());
        
        if (history.size() > 1) {
            CreditScore oldest = history.get(history.size() - 1);
            int totalChange = latest.getScore() - oldest.getScore();
            analysis.put("totalChange", totalChange);
            analysis.put("trend", totalChange > 0 ? "IMPROVING" : totalChange < 0 ? "DECLINING" : "STABLE");
        }
        
        analysis.put("history", history);
        analysis.put("recommendations", generateRecommendations(latest));
        
        return analysis;
    }
    
    private List<String> generateRecommendations(CreditScore score) {
        List<String> recommendations = new ArrayList<>();
        
        if (score.getScore() < 670) {
            recommendations.add("Pay all bills on time to improve payment history");
            recommendations.add("Reduce credit card balances below 30% of limits");
            recommendations.add("Avoid opening multiple new credit accounts");
        } else if (score.getScore() < 740) {
            recommendations.add("Maintain low credit utilization ratio");
            recommendations.add("Keep old credit accounts open");
            recommendations.add("Monitor credit report for errors");
        } else {
            recommendations.add("Continue excellent credit management practices");
            recommendations.add("Maintain diverse credit mix");
            recommendations.add("Review credit report annually");
        }
        
        return recommendations;
    }
}
