package com.investments.stocks.repo;

import com.investments.stocks.data.PortfolioTransaction;
import com.investments.stocks.data.PortfolioTransaction.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for PortfolioTransaction entity
 */
@Repository
public interface PortfolioTransactionRepository extends JpaRepository<PortfolioTransaction, Long> {

    /**
     * Find all transactions for a specific user
     */
    List<PortfolioTransaction> findByUserIdOrderByTransactionDateDesc(Long userId);

    /**
     * Find transactions by user and symbol
     */
    List<PortfolioTransaction> findByUserIdAndSymbolOrderByTransactionDateAsc(Long userId, String symbol);

    /**
     * Find BUY transactions for a user and symbol (for FIFO calculation)
     */
    List<PortfolioTransaction> findByUserIdAndSymbolAndTransactionTypeOrderByTransactionDateAsc(
            Long userId, String symbol, TransactionType transactionType);

    /**
     * Find transactions within a date range
     */
    @Query("SELECT pt FROM PortfolioTransaction pt WHERE pt.userId = :userId " +
           "AND pt.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY pt.transactionDate DESC")
    List<PortfolioTransaction> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Calculate total realized gains for a user
     */
    @Query("SELECT COALESCE(SUM(pt.realizedGain), 0) FROM PortfolioTransaction pt " +
           "WHERE pt.userId = :userId AND pt.transactionType = 'SELL'")
    BigDecimal calculateTotalRealizedGains(@Param("userId") Long userId);

    /**
     * Calculate total realized gains for a specific symbol
     */
    @Query("SELECT COALESCE(SUM(pt.realizedGain), 0) FROM PortfolioTransaction pt " +
           "WHERE pt.userId = :userId AND pt.symbol = :symbol AND pt.transactionType = 'SELL'")
    BigDecimal calculateRealizedGainsBySymbol(@Param("userId") Long userId, @Param("symbol") String symbol);

    /**
     * Get transaction statistics for a user
     */
    @Query("SELECT pt.transactionType, COUNT(pt), SUM(pt.totalAmount) FROM PortfolioTransaction pt " +
           "WHERE pt.userId = :userId GROUP BY pt.transactionType")
    List<Object[]> getTransactionStatistics(@Param("userId") Long userId);

    /**
     * Find latest transaction date for a symbol
     */
    @Query("SELECT MAX(pt.transactionDate) FROM PortfolioTransaction pt " +
           "WHERE pt.userId = :userId AND pt.symbol = :symbol")
    LocalDate findLatestTransactionDate(@Param("userId") Long userId, @Param("symbol") String symbol);

    /**
     * Count total transactions for a user
     */
    long countByUserId(Long userId);

    /**
     * Delete all transactions for a user and symbol
     */
    void deleteByUserIdAndSymbol(Long userId, String symbol);
}
