package com.stocks.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stocks.data.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {
}
