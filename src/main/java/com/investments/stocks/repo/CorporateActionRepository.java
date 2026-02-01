package com.investments.stocks.repo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.investments.stocks.data.CorporateAction;
import com.investments.stocks.data.CorporateActionType;

@Repository
public interface CorporateActionRepository extends JpaRepository<CorporateAction, Long> {

    List<CorporateAction> findBySymbolOrderByExDateDesc(String symbol);

    List<CorporateAction> findBySymbolAndActionType(String symbol, CorporateActionType actionType);

    @Query("SELECT ca FROM CorporateAction ca WHERE ca.symbol = :symbol AND ca.exDate BETWEEN :startDate AND :endDate ORDER BY ca.exDate DESC")
    List<CorporateAction> findBySymbolAndDateRange(@Param("symbol") String symbol, 
                                                     @Param("startDate") LocalDate startDate, 
                                                     @Param("endDate") LocalDate endDate);

    @Query("SELECT ca FROM CorporateAction ca WHERE ca.exDate >= :fromDate ORDER BY ca.exDate ASC")
    List<CorporateAction> findUpcomingActions(@Param("fromDate") LocalDate fromDate);
}
