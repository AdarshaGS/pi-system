package com.pisystem.modules.stocks.diversification.portfolio.service;

import com.pisystem.modules.stocks.diversification.portfolio.data.PortfolioDTOResponse;

public interface PortfolioReadService {

    PortfolioDTOResponse getPortfolioSummary(Long userId);
}
