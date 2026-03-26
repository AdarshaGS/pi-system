package com.investments.stocks.repo;

import com.investments.stocks.data.CreditScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditScoreRepository extends JpaRepository<CreditScore, Long> {
    
    List<CreditScore> findByUserIdOrderByRecordDateDesc(Long userId);
    
    List<CreditScore> findByUserIdAndProviderOrderByRecordDateDesc(Long userId, String provider);
    
    @Query("SELECT c FROM CreditScore c WHERE c.userId = :userId ORDER BY c.recordDate DESC LIMIT 1")
    Optional<CreditScore> findLatestScore(@Param("userId") Long userId);
    
    @Query("SELECT c FROM CreditScore c WHERE c.userId = :userId AND c.provider = :provider ORDER BY c.recordDate DESC LIMIT 1")
    Optional<CreditScore> findLatestScoreByProvider(@Param("userId") Long userId, @Param("provider") String provider);
}
