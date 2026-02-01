package com.tax.repo;

import com.tax.data.TDSEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TDSRepository extends JpaRepository<TDSEntry, Long>, JpaSpecificationExecutor<TDSEntry> {
    
    List<TDSEntry> findByUserIdAndFinancialYear(Long userId, String financialYear);
    
    List<TDSEntry> findByUserId(Long userId);
    
    List<TDSEntry> findByUserIdAndFinancialYearAndQuarter(Long userId, String financialYear, Integer quarter);
    
    List<TDSEntry> findByUserIdAndFinancialYearAndReconciliationStatus(Long userId, String financialYear, String reconciliationStatus);
    
    List<TDSEntry> findByUserIdAndFinancialYearOrderByTdsDepositedDateDesc(Long userId, String financialYear);
    
    List<TDSEntry> findByDeductorTan(String deductorTan);
    
    @Query("SELECT SUM(tds.tdsDeducted) FROM TDSEntry tds WHERE tds.userId = :userId AND tds.financialYear = :financialYear")
    BigDecimal getTotalTDSDeducted(@Param("userId") Long userId, @Param("financialYear") String financialYear);
    
    @Query("SELECT SUM(tds.tdsDeducted) FROM TDSEntry tds WHERE tds.userId = :userId AND tds.financialYear = :financialYear AND tds.section = :section")
    BigDecimal getTotalTDSBySection(@Param("userId") Long userId, @Param("financialYear") String financialYear, @Param("section") String section);
    
    @Query("SELECT COUNT(tds) FROM TDSEntry tds WHERE tds.userId = :userId AND tds.financialYear = :financialYear AND tds.reconciliationStatus = 'MISMATCHED'")
    Long countMismatchedEntries(@Param("userId") Long userId, @Param("financialYear") String financialYear);
    
    @Query("SELECT SUM(tds.differenceAmount) FROM TDSEntry tds WHERE tds.userId = :userId AND tds.financialYear = :financialYear AND tds.reconciliationStatus = 'MISMATCHED'")
    BigDecimal getTotalDifferenceAmount(@Param("userId") Long userId, @Param("financialYear") String financialYear);
    
    List<TDSEntry> findByUserIdAndIsMatchedWith26as(Long userId, Boolean isMatchedWith26as);
}
