package com.investments.stocks.diversification.recommendations.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.investments.stocks.diversification.recommendations.data.Recommendation;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

}
