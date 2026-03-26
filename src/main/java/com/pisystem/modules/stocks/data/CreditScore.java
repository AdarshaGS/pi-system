package com.investments.stocks.data;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_scores")
public class CreditScore {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Integer score;
    
    @Column(nullable = false, length = 50)
    private String provider; // e.g., "EQUIFAX", "EXPERIAN", "TRANSUNION"
    
    @Column(nullable = false)
    private LocalDateTime recordDate;
    
    @Column(length = 20)
    private String scoreRating; // e.g., "EXCELLENT", "GOOD", "FAIR", "POOR"
    
    @Column
    private Integer changeFromPrevious;
    
    @Column(length = 1000)
    private String factors; // JSON or comma-separated key factors
    
    @Column(length = 1000)
    private String recommendations;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        calculateRating();
    }
    
    public void calculateRating() {
        if (score >= 800) {
            scoreRating = "EXCELLENT";
        } else if (score >= 740) {
            scoreRating = "VERY_GOOD";
        } else if (score >= 670) {
            scoreRating = "GOOD";
        } else if (score >= 580) {
            scoreRating = "FAIR";
        } else {
            scoreRating = "POOR";
        }
    }
    
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
    
    public Integer getScore() {
        return score;
    }
    
    public void setScore(Integer score) {
        this.score = score;
    }
    
    public String getProvider() {
        return provider;
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public LocalDateTime getRecordDate() {
        return recordDate;
    }
    
    public void setRecordDate(LocalDateTime recordDate) {
        this.recordDate = recordDate;
    }
    
    public String getScoreRating() {
        return scoreRating;
    }
    
    public void setScoreRating(String scoreRating) {
        this.scoreRating = scoreRating;
    }
    
    public Integer getChangeFromPrevious() {
        return changeFromPrevious;
    }
    
    public void setChangeFromPrevious(Integer changeFromPrevious) {
        this.changeFromPrevious = changeFromPrevious;
    }
    
    public String getFactors() {
        return factors;
    }
    
    public void setFactors(String factors) {
        this.factors = factors;
    }
    
    public String getRecommendations() {
        return recommendations;
    }
    
    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
