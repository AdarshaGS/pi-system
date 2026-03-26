package com.investments.stocks.diversification.portfolio.service;

import com.investments.stocks.diversification.portfolio.data.PortfolioDTOResponse;

public interface PortfolioReadService {

    PortfolioDTOResponse getPortfolioSummary(Long userId);
}
