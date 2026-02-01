package com.investments.stocks.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.investments.stocks.data.PriceAlert;

@Repository
public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {

    List<PriceAlert> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<PriceAlert> findByUserIdAndSymbol(Long userId, String symbol);

    List<PriceAlert> findBySymbolAndIsActiveTrue(String symbol);

    List<PriceAlert> findByUserIdAndIsActiveTrue(Long userId);
}
