package com.investments.stocks.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.investments.stocks.data.StockPrice;

@Repository
public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {

    Optional<StockPrice> findBySymbolAndPriceDate(String symbol, LocalDate priceDate);

    List<StockPrice> findBySymbolOrderByPriceDateDesc(String symbol);

    @Query("SELECT sp FROM StockPrice sp WHERE sp.symbol = :symbol AND sp.priceDate BETWEEN :startDate AND :endDate ORDER BY sp.priceDate ASC")
    List<StockPrice> findBySymbolAndDateRange(@Param("symbol") String symbol, 
                                               @Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT sp FROM StockPrice sp WHERE sp.symbol = :symbol ORDER BY sp.priceDate DESC LIMIT 1")
    Optional<StockPrice> findLatestBySymbol(@Param("symbol") String symbol);
}
