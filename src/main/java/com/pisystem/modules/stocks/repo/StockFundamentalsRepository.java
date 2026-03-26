package com.investments.stocks.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.investments.stocks.data.StockFundamentals;

@Repository
public interface StockFundamentalsRepository extends JpaRepository<StockFundamentals, Long> {

    Optional<StockFundamentals> findBySymbol(String symbol);

    void deleteBySymbol(String symbol);
}
