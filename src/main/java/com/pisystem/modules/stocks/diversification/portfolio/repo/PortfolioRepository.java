package com.pisystem.modules.stocks.diversification.portfolio.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pisystem.modules.stocks.diversification.portfolio.data.Portfolio;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findByUserId(Long userId);

}
