package com.stocks.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stocks.data.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
}
