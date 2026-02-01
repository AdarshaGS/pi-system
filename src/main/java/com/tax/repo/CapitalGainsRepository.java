package com.tax.repo;

import com.tax.data.CapitalGainsTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CapitalGainsRepository extends JpaRepository<CapitalGainsTransaction, Long>, JpaSpecificationExecutor<CapitalGainsTransaction> {
    List<CapitalGainsTransaction> findByUserIdAndFinancialYear(Long userId, String financialYear);
    List<CapitalGainsTransaction> findByUserId(Long userId);
}
