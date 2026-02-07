package com.mutualfund.repository;

import com.mutualfund.model.MutualFundHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MutualFundHoldingRepository extends JpaRepository<MutualFundHolding, Long> {
    
    List<MutualFundHolding> findByUserId(Long userId);
    
    Optional<MutualFundHolding> findByUserIdAndMutualFundIdAndFolioNumber(Long userId, Long mutualFundId, String folioNumber);
    
    Optional<MutualFundHolding> findByUserIdAndMutualFundId(Long userId, Long mutualFundId);
}
