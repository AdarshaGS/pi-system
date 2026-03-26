package com.investments.stocks.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.investments.stocks.data.StockWatchlist;

@Repository
public interface StockWatchlistRepository extends JpaRepository<StockWatchlist, Long> {

    List<StockWatchlist> findByUserIdOrderByAddedAtDesc(Long userId);

    Optional<StockWatchlist> findByUserIdAndSymbol(Long userId, String symbol);

    void deleteByUserIdAndSymbol(Long userId, String symbol);

    boolean existsByUserIdAndSymbol(Long userId, String symbol);
}
