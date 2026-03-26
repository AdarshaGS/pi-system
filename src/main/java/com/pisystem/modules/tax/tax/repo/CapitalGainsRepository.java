package com.tax.repo;

import com.tax.data.CapitalGainsTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CapitalGainsRepository extends JpaRepository<CapitalGainsTransaction, Long>, JpaSpecificationExecutor<CapitalGainsTransaction> {
    
    List<CapitalGainsTransaction> findByUserIdAndFinancialYear(Long userId, String financialYear);
    
    List<CapitalGainsTransaction> findByUserId(Long userId);
    
    List<CapitalGainsTransaction> findByUserIdAndFinancialYearAndGainType(Long userId, String financialYear, String gainType);
    
    List<CapitalGainsTransaction> findByUserIdAndFinancialYearAndAssetType(Long userId, String financialYear, String assetType);
    
    List<CapitalGainsTransaction> findByUserIdAndFinancialYearOrderBySaleDateDesc(Long userId, String financialYear);
    
    @Query("SELECT SUM(cg.capitalGain) FROM CapitalGainsTransaction cg WHERE cg.userId = :userId AND cg.financialYear = :financialYear AND cg.gainType = :gainType")
    BigDecimal getTotalCapitalGainByType(@Param("userId") Long userId, @Param("financialYear") String financialYear, @Param("gainType") String gainType);
    
    @Query("SELECT SUM(cg.taxAmount) FROM CapitalGainsTransaction cg WHERE cg.userId = :userId AND cg.financialYear = :financialYear")
    BigDecimal getTotalTaxAmount(@Param("userId") Long userId, @Param("financialYear") String financialYear);
    
    @Query("SELECT cg FROM CapitalGainsTransaction cg WHERE cg.userId = :userId AND cg.capitalGain < 0 AND cg.isSetOff = false")
    List<CapitalGainsTransaction> findUnsetOffLosses(@Param("userId") Long userId);
}
