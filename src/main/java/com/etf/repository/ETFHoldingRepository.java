package com.etf.repository;

import com.etf.model.ETFHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ETFHoldingRepository extends JpaRepository<ETFHolding, Long> {
    
    List<ETFHolding> findByUserId(Long userId);
    
    Optional<ETFHolding> findByUserIdAndEtfId(Long userId, Long etfId);
}
