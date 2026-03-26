package com.protection.insurance.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.protection.insurance.data.ClaimStatus;
import com.protection.insurance.data.InsuranceClaim;

@Repository
public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, Long> {

    List<InsuranceClaim> findByInsuranceIdOrderByClaimDateDesc(Long insuranceId);

    List<InsuranceClaim> findByInsuranceIdAndClaimStatus(Long insuranceId, ClaimStatus status);

    @Query("SELECT COUNT(c) FROM InsuranceClaim c WHERE c.insuranceId = :insuranceId AND c.claimStatus = :status")
    Long countByInsuranceIdAndClaimStatus(@Param("insuranceId") Long insuranceId, @Param("status") ClaimStatus status);

    @Query("SELECT SUM(c.approvedAmount) FROM InsuranceClaim c WHERE c.insuranceId = :insuranceId AND c.claimStatus = 'SETTLED'")
    java.math.BigDecimal getTotalSettledAmount(@Param("insuranceId") Long insuranceId);
}
