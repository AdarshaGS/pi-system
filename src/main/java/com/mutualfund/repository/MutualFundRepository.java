package com.mutualfund.repository;

import com.mutualfund.model.MutualFund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MutualFundRepository extends JpaRepository<MutualFund, Long> {
    
    Optional<MutualFund> findBySchemeCode(String schemeCode);
    
    List<MutualFund> findByFundHouse(String fundHouse);
    
    List<MutualFund> findBySchemeCategory(String schemeCategory);
    
    List<MutualFund> findBySchemeNameContainingIgnoreCase(String schemeName);
}
