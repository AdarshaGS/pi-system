package com.investments.stocks.diversification.portfolio.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.investments.stocks.diversification.portfolio.data.Portfolio;
import com.investments.stocks.diversification.portfolio.repo.PortfolioRepository;
import com.investments.stocks.exception.SymbolNotFoundException;
import com.investments.stocks.repo.StockRepository;
import com.investments.stocks.service.StockReadService;
import com.common.data.EntityType;
import com.common.subscription.SubscriptionTierService;

@Service
public class PortfolioWriteServiceImpl implements PortfolioWriteService {

    private final PortfolioRepository portfolioRepository;
    // private final StockRepository stockRepository;
    private final StockReadService stockReadService;
    
    @Autowired
    private SubscriptionTierService subscriptionTierService;

    public PortfolioWriteServiceImpl(PortfolioRepository portfolioRepository, StockRepository stockRepository,
            StockReadService stockReadService) {
        this.portfolioRepository = portfolioRepository;
        // this.stockRepository = stockRepository;
        this.stockReadService = stockReadService;
    }

    @Override
    @Transactional
    public Portfolio addPortfolio(Portfolio portfolio) {

        // Check tier limit before adding
        int currentCount = portfolioRepository.findByUserId(portfolio.getUserId()).size();
        subscriptionTierService.checkStockLimit(portfolio.getUserId(), currentCount);

        Long stockId = this.stockReadService.getStockBySymbol(portfolio.getEntityName()).getId();
        if (stockId == null) {
            throw new SymbolNotFoundException("Symbol not found: " + portfolio.getStockSymbol());
        }

        // calculate profit_and_loss percentage
        BigDecimal profitAndLoss = portfolio.getCurrentPrice().subtract(portfolio.getPurchasePrice());
        BigDecimal profitAndLossPercentage = profitAndLoss.divide(portfolio.getPurchasePrice(), 2,
                BigDecimal.ROUND_HALF_UP);

        Portfolio portfolioBuilder = Portfolio.builder()
                .userId(portfolio.getUserId())
                .entityType(portfolio.getEntityType() != null ? portfolio.getEntityType() : EntityType.STOCK)
                .stockId(stockId)
                .stockSymbol(portfolio.getStockSymbol())
                .quantity(portfolio.getQuantity())
                .purchasePrice(portfolio.getPurchasePrice())
                .currentPrice(portfolio.getCurrentPrice())
                .profitAndLossPercentage(profitAndLossPercentage)
                .build();
        return this.portfolioRepository.save(portfolioBuilder);
    }

}
