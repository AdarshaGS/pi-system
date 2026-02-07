package com.investments.stocks.repo;

import com.investments.stocks.data.CashFlowRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CashFlowRecordRepository extends JpaRepository<CashFlowRecord, Long> {
    
    List<CashFlowRecord> findByUserId(Long userId);
    
    List<CashFlowRecord> findByUserIdAndPeriodType(Long userId, CashFlowRecord.PeriodType periodType);
    
    @Query("SELECT c FROM CashFlowRecord c WHERE c.userId = :userId AND c.recordDate BETWEEN :startDate AND :endDate ORDER BY c.recordDate")
    List<CashFlowRecord> findByDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT c FROM CashFlowRecord c WHERE c.userId = :userId ORDER BY c.recordDate DESC LIMIT 12")
    List<CashFlowRecord> findRecentRecords(@Param("userId") Long userId);
}
