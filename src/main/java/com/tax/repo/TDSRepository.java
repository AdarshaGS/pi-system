package com.tax.repo;

import com.tax.data.TDSEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TDSRepository extends JpaRepository<TDSEntry, Long>, JpaSpecificationExecutor<TDSEntry> {
    List<TDSEntry> findByUserIdAndFinancialYear(Long userId, String financialYear);
    List<TDSEntry> findByUserId(Long userId);
}
