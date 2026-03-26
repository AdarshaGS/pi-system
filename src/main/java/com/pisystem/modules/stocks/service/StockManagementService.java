package com.investments.stocks.service;

import com.investments.stocks.data.*;
import com.investments.stocks.dto.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface StockManagementService {

    // Stock CRUD Operations
    Stock createStock(CreateStockDTO request);
    Stock updateStock(String symbol, CreateStockDTO request);
    void deleteStock(String symbol);
    List<Stock> getAllStocks();
    List<Stock> searchStocks(String query);

    // Price History
    void savePriceData(StockPrice priceData);
    PriceHistoryResponse getPriceHistory(String symbol, LocalDate startDate, LocalDate endDate);
    StockPrice getLatestPrice(String symbol);

    // Fundamentals
    StockFundamentals saveFundamentals(StockFundamentals fundamentals);
    StockFundamentals getFundamentals(String symbol);

    // Watchlist
    StockWatchlist addToWatchlist(Long userId, String symbol, String notes);
    void removeFromWatchlist(Long userId, String symbol);
    WatchlistResponse getUserWatchlist(Long userId);

    // Price Alerts
    PriceAlert createAlert(Long userId, CreateAlertRequest request);
    List<PriceAlert> getUserAlerts(Long userId);
    void deleteAlert(Long alertId, Long userId);
    void checkAndTriggerAlerts(String symbol, BigDecimal currentPrice);

    // Corporate Actions
    CorporateAction saveCorporateAction(CorporateAction action);
    CorporateActionsResponse getCorporateActions(String symbol);
    List<CorporateAction> getUpcomingActions();
}
