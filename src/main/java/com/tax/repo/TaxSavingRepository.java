package com.tax.repo;

import com.tax.data.TaxSavingInvestment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxSavingRepository extends JpaRepository<TaxSavingInvestment, Long>, JpaSpecificationExecutor<TaxSavingInvestment> {
    List<TaxSavingInvestment> findByUserIdAndFinancialYear(Long userId, String financialYear);
}
