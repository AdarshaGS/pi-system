package com.etf.repository;

import com.etf.model.ETF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ETFRepository extends JpaRepository<ETF, Long> {
    
    Optional<ETF> findBySymbol(String symbol);
    
    Optional<ETF> findByIsin(String isin);
    
    List<ETF> findByEtfType(String etfType);
    
    List<ETF> findByExchange(String exchange);
    
    List<ETF> findByNameContainingIgnoreCase(String name);
}
