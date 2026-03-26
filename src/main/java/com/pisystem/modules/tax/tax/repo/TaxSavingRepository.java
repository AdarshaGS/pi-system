package com.tax.repo;

import com.tax.data.TaxSavingInvestment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TaxSavingRepository extends JpaRepository<TaxSavingInvestment, Long>, JpaSpecificationExecutor<TaxSavingInvestment> {
    
    List<TaxSavingInvestment> findByUserIdAndFinancialYear(Long userId, String financialYear);
    
    List<TaxSavingInvestment> findByUserIdAndFinancialYearAndInvestmentType(Long userId, String financialYear, String investmentType);
    
    List<TaxSavingInvestment> findByUserIdAndFinancialYearOrderByInvestmentDateDesc(Long userId, String financialYear);
    
    @Query("SELECT SUM(tsi.amount) FROM TaxSavingInvestment tsi WHERE tsi.userId = :userId AND tsi.financialYear = :financialYear AND tsi.investmentType = :investmentType")
    BigDecimal getTotalInvestmentByType(@Param("userId") Long userId, @Param("financialYear") String financialYear, @Param("investmentType") String investmentType);
    
    @Query("SELECT SUM(tsi.amount) FROM TaxSavingInvestment tsi WHERE tsi.userId = :userId AND tsi.financialYear = :financialYear")
    BigDecimal getTotalInvestment(@Param("userId") Long userId, @Param("financialYear") String financialYear);
    
    List<TaxSavingInvestment> findByLinkedEntityTypeAndLinkedEntityId(String linkedEntityType, Long linkedEntityId);
    
    List<TaxSavingInvestment> findByUserIdAndIsAutoPopulated(Long userId, Boolean isAutoPopulated);
}
