package com.protection.insurance.repo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.protection.insurance.data.InsurancePremium;
import com.protection.insurance.data.PremiumPaymentStatus;

@Repository
public interface InsurancePremiumRepository extends JpaRepository<InsurancePremium, Long> {

    List<InsurancePremium> findByInsuranceIdOrderByPaymentDateDesc(Long insuranceId);

    List<InsurancePremium> findByInsuranceIdAndPaymentStatus(Long insuranceId, PremiumPaymentStatus status);

    @Query("SELECT COUNT(p) FROM InsurancePremium p WHERE p.insuranceId = :insuranceId AND p.paymentStatus = :status")
    Long countByInsuranceIdAndPaymentStatus(@Param("insuranceId") Long insuranceId, @Param("status") PremiumPaymentStatus status);

    @Query("SELECT p FROM InsurancePremium p WHERE p.insuranceId = :insuranceId AND p.paymentDate BETWEEN :startDate AND :endDate")
    List<InsurancePremium> findPremiumsByDateRange(@Param("insuranceId") Long insuranceId, 
                                                     @Param("startDate") LocalDate startDate, 
                                                     @Param("endDate") LocalDate endDate);
}
