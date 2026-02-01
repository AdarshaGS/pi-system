package com.investments.stocks.service;

import com.common.security.AuthenticationHelper;
import com.investments.stocks.data.*;
import com.investments.stocks.dto.*;
import com.investments.stocks.repo.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockManagementServiceImpl implements StockManagementService {

    private final StockRepository stockRepository;
    private final StockPriceRepository priceRepository;
    private final StockFundamentalsRepository fundamentalsRepository;
    private final StockWatchlistRepository watchlistRepository;
    private final PriceAlertRepository alertRepository;
    private final CorporateActionRepository corporateActionRepository;
    private final AuthenticationHelper authenticationHelper;

    // ==================== Stock CRUD Operations ====================

    @Override
    @Transactional
    public Stock createStock(CreateStockDTO request) {
        authenticationHelper.validateAdminAccess();

        Stock stock = Stock.builder()
                .symbol(request.getSymbol().toUpperCase())
                .companyName(request.getCompanyName())
                .price(request.getPrice() != null ? request.getPrice().doubleValue() : null)
                .description(request.getDescription())
                .sectorId(request.getSectorId())
                .marketCap(request.getMarketCap() != null ? request.getMarketCap().doubleValue() : null)
                .build();

        return stockRepository.save(stock);
    }

    @Override
    @Transactional
    public Stock updateStock(String symbol, CreateStockDTO request) {
        authenticationHelper.validateAdminAccess();

        Stock stock = stockRepository.findBySymbol(symbol.toUpperCase());
        if (stock == null) {
            throw new RuntimeException("Stock not found: " + symbol);
        }

        stock.setCompanyName(request.getCompanyName());
        stock.setPrice(request.getPrice() != null ? request.getPrice().doubleValue() : stock.getPrice());
        stock.setDescription(request.getDescription());
        stock.setSectorId(request.getSectorId());
        stock.setMarketCap(request.getMarketCap() != null ? request.getMarketCap().doubleValue() : stock.getMarketCap());

        return stockRepository.save(stock);
    }

    @Override
    @Transactional
    public void deleteStock(String symbol) {
        authenticationHelper.validateAdminAccess();

        Stock stock = stockRepository.findBySymbol(symbol.toUpperCase());
        if (stock != null) {
            stockRepository.delete(stock);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Stock> searchStocks(String query) {
        // Simple search implementation - can be enhanced with Elasticsearch
        return stockRepository.findAll().stream()
                .filter(stock -> stock.getSymbol().contains(query.toUpperCase()) ||
                                stock.getCompanyName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    // ==================== Price History ====================

    @Override
    @Transactional
    public void savePriceData(StockPrice priceData) {
        priceRepository.save(priceData);
    }

    @Override
    @Transactional(readOnly = true)
    public PriceHistoryResponse getPriceHistory(String symbol, LocalDate startDate, LocalDate endDate) {
        List<StockPrice> prices = priceRepository.findBySymbolAndDateRange(symbol.toUpperCase(), startDate, endDate);

        List<PriceHistoryResponse.PriceData> priceDataList = prices.stream()
                .map(p -> PriceHistoryResponse.PriceData.builder()
                        .date(p.getPriceDate())
                        .open(p.getOpenPrice())
                        .high(p.getHighPrice())
                        .low(p.getLowPrice())
                        .close(p.getClosePrice())
                        .volume(p.getVolume())
                        .build())
                .collect(Collectors.toList());

        return PriceHistoryResponse.builder()
                .symbol(symbol.toUpperCase())
                .startDate(startDate)
                .endDate(endDate)
                .totalRecords(priceDataList.size())
                .prices(priceDataList)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StockPrice getLatestPrice(String symbol) {
        return priceRepository.findLatestBySymbol(symbol.toUpperCase()).orElse(null);
    }

    // ==================== Fundamentals ====================

    @Override
    @Transactional
    public StockFundamentals saveFundamentals(StockFundamentals fundamentals) {
        authenticationHelper.validateAdminAccess();
        return fundamentalsRepository.save(fundamentals);
    }

    @Override
    @Transactional(readOnly = true)
    public StockFundamentals getFundamentals(String symbol) {
        return fundamentalsRepository.findBySymbol(symbol.toUpperCase()).orElse(null);
    }

    // ==================== Watchlist ====================

    @Override
    @Transactional
    public StockWatchlist addToWatchlist(Long userId, String symbol, String notes) {
        authenticationHelper.validateUserAccess(userId);

        // Check if already exists
        if (watchlistRepository.existsByUserIdAndSymbol(userId, symbol.toUpperCase())) {
            throw new RuntimeException("Stock already in watchlist");
        }

        StockWatchlist watchlist = StockWatchlist.builder()
                .userId(userId)
                .symbol(symbol.toUpperCase())
                .notes(notes)
                .build();

        return watchlistRepository.save(watchlist);
    }

    @Override
    @Transactional
    public void removeFromWatchlist(Long userId, String symbol) {
        authenticationHelper.validateUserAccess(userId);
        watchlistRepository.deleteByUserIdAndSymbol(userId, symbol.toUpperCase());
    }

    @Override
    @Transactional(readOnly = true)
    public WatchlistResponse getUserWatchlist(Long userId) {
        authenticationHelper.validateUserAccess(userId);

        List<StockWatchlist> watchlist = watchlistRepository.findByUserIdOrderByAddedAtDesc(userId);

        List<WatchlistResponse.WatchlistItem> items = watchlist.stream()
                .map(w -> {
                    Stock stock = stockRepository.findBySymbol(w.getSymbol());
                    StockPrice latestPrice = getLatestPrice(w.getSymbol());

                    return WatchlistResponse.WatchlistItem.builder()
                            .id(w.getId())
                            .symbol(w.getSymbol())
                            .companyName(stock != null ? stock.getCompanyName() : "Unknown")
                            .currentPrice(latestPrice != null ? latestPrice.getClosePrice() : BigDecimal.ZERO)
                            .change(BigDecimal.ZERO) // Calculate based on previous close
                            .changePercent(BigDecimal.ZERO)
                            .notes(w.getNotes())
                            .addedAt(w.getAddedAt().toString())
                            .build();
                })
                .collect(Collectors.toList());

        return WatchlistResponse.builder()
                .userId(userId)
                .totalStocks(items.size())
                .stocks(items)
                .build();
    }

    // ==================== Price Alerts ====================

    @Override
    @Transactional
    public PriceAlert createAlert(Long userId, CreateAlertRequest request) {
        authenticationHelper.validateUserAccess(userId);

        PriceAlert alert = PriceAlert.builder()
                .userId(userId)
                .symbol(request.getSymbol().toUpperCase())
                .alertType(request.getAlertType())
                .targetPrice(request.getTargetPrice())
                .percentageChange(request.getPercentageChange())
                .isTriggered(false)
                .isActive(true)
                .build();

        return alertRepository.save(alert);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceAlert> getUserAlerts(Long userId) {
        authenticationHelper.validateUserAccess(userId);
        return alertRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public void deleteAlert(Long alertId, Long userId) {
        authenticationHelper.validateUserAccess(userId);

        PriceAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        if (!alert.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        alertRepository.delete(alert);
    }

    @Override
    @Transactional
    public void checkAndTriggerAlerts(String symbol, BigDecimal currentPrice) {
        List<PriceAlert> activeAlerts = alertRepository.findBySymbolAndIsActiveTrue(symbol.toUpperCase());

        for (PriceAlert alert : activeAlerts) {
            boolean triggered = false;

            switch (alert.getAlertType()) {
                case TARGET_PRICE:
                    if (alert.getTargetPrice() != null && 
                        currentPrice.compareTo(alert.getTargetPrice()) >= 0) {
                        triggered = true;
                    }
                    break;

                case PERCENTAGE_UP:
                case PERCENTAGE_DOWN:
                    // Would need base price to calculate percentage change
                    // Implementation depends on how base price is stored
                    break;
            }

            if (triggered) {
                alert.setIsTriggered(true);
                alert.setTriggeredAt(LocalDateTime.now());
                alert.setIsActive(false);
                alertRepository.save(alert);

                // TODO: Send notification to user
            }
        }
    }

    // ==================== Corporate Actions ====================

    @Override
    @Transactional
    public CorporateAction saveCorporateAction(CorporateAction action) {
        authenticationHelper.validateAdminAccess();
        return corporateActionRepository.save(action);
    }

    @Override
    @Transactional(readOnly = true)
    public CorporateActionsResponse getCorporateActions(String symbol) {
        List<CorporateAction> actions = corporateActionRepository.findBySymbolOrderByExDateDesc(symbol.toUpperCase());

        List<CorporateActionsResponse.ActionItem> actionItems = actions.stream()
                .map(a -> CorporateActionsResponse.ActionItem.builder()
                        .id(a.getId())
                        .actionType(a.getActionType().name())
                        .announcementDate(a.getAnnouncementDate())
                        .exDate(a.getExDate())
                        .recordDate(a.getRecordDate())
                        .paymentDate(a.getPaymentDate())
                        .dividendAmount(a.getDividendAmount())
                        .splitRatio(a.getSplitRatio())
                        .bonusRatio(a.getBonusRatio())
                        .rightsRatio(a.getRightsRatio())
                        .rightsPrice(a.getRightsPrice())
                        .description(a.getDescription())
                        .build())
                .collect(Collectors.toList());

        return CorporateActionsResponse.builder()
                .symbol(symbol.toUpperCase())
                .totalActions(actionItems.size())
                .actions(actionItems)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CorporateAction> getUpcomingActions() {
        return corporateActionRepository.findUpcomingActions(LocalDate.now());
    }
}
