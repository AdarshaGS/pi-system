package com.investments.stocks.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import com.common.features.FeatureFlag;
import com.common.features.RequiresFeature;
import com.common.security.AuthenticationHelper;
import com.investments.stocks.data.*;
import com.investments.stocks.dto.*;
import com.investments.stocks.service.StockManagementService;
import com.investments.stocks.service.StockReadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stocks")
@RequiresFeature(FeatureFlag.STOCKS)
@Tag(name = "Stock Management", description = "APIs for stock management, price tracking, watchlist and alerts")
@RequiredArgsConstructor
public class StockController {

    private final StockReadService stockReadService;
    private final StockManagementService stockManagementService;
    private final AuthenticationHelper authenticationHelper;

    @GetMapping("/{symbol}")
    @Operation(summary = "Get stock by symbol", description = "Fetches stock details including price and sector.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved stock details")
    @ApiResponse(responseCode = "404", description = "Stock symbol not found")
    public StockResponse getStockBySymbol(@PathVariable("symbol") String symbol) {
        return this.stockReadService.getStockBySymbol(symbol);
    }

    // ==================== Stock CRUD Operations ====================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new stock", description = "Admin-only endpoint to create a new stock")
    public ResponseEntity<Stock> createStock(@Valid @RequestBody CreateStockDTO request) {
        Stock stock = stockManagementService.createStock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(stock);
    }

    @PutMapping("/{symbol}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update stock", description = "Admin-only endpoint to update stock details")
    public ResponseEntity<Stock> updateStock(@PathVariable String symbol, @Valid @RequestBody CreateStockDTO request) {
        Stock stock = stockManagementService.updateStock(symbol, request);
        return ResponseEntity.ok(stock);
    }

    @DeleteMapping("/{symbol}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete stock", description = "Admin-only endpoint to delete a stock")
    public ResponseEntity<Void> deleteStock(@PathVariable String symbol) {
        stockManagementService.deleteStock(symbol);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get all stocks", description = "Retrieve all stocks in the system")
    public ResponseEntity<List<Stock>> getAllStocks() {
        return ResponseEntity.ok(stockManagementService.getAllStocks());
    }

    @GetMapping("/search")
    @Operation(summary = "Search stocks", description = "Search stocks by symbol or company name")
    public ResponseEntity<List<Stock>> searchStocks(@RequestParam String query) {
        return ResponseEntity.ok(stockManagementService.searchStocks(query));
    }

    // ==================== Price History ====================

    @GetMapping("/{symbol}/price-history")
    @Operation(summary = "Get price history", description = "Retrieve historical price data for a stock")
    public ResponseEntity<PriceHistoryResponse> getPriceHistory(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(stockManagementService.getPriceHistory(symbol, startDate, endDate));
    }

    @PostMapping("/{symbol}/prices")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Save price data", description = "Admin-only endpoint to save OHLC price data")
    public ResponseEntity<Void> savePriceData(@PathVariable String symbol, @Valid @RequestBody StockPrice priceData) {
        priceData.setSymbol(symbol.toUpperCase());
        stockManagementService.savePriceData(priceData);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // ==================== Fundamentals ====================

    @GetMapping("/{symbol}/fundamentals")
    @Operation(summary = "Get stock fundamentals", description = "Retrieve fundamental data like PE ratio, market cap, etc.")
    public ResponseEntity<StockFundamentals> getFundamentals(@PathVariable String symbol) {
        StockFundamentals fundamentals = stockManagementService.getFundamentals(symbol);
        if (fundamentals == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(fundamentals);
    }

    @PostMapping("/{symbol}/fundamentals")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Save stock fundamentals", description = "Admin-only endpoint to save fundamental data")
    public ResponseEntity<StockFundamentals> saveFundamentals(@PathVariable String symbol, @Valid @RequestBody StockFundamentals fundamentals) {
        fundamentals.setSymbol(symbol.toUpperCase());
        StockFundamentals saved = stockManagementService.saveFundamentals(fundamentals);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ==================== Watchlist ====================

    @PostMapping("/watchlist")
    @Operation(summary = "Add to watchlist", description = "Add a stock to user's watchlist")
    public ResponseEntity<StockWatchlist> addToWatchlist(
            @RequestParam String symbol,
            @RequestParam(required = false) String notes) {
        Long userId = authenticationHelper.getCurrentUserId();
        StockWatchlist watchlist = stockManagementService.addToWatchlist(userId, symbol, notes);
        return ResponseEntity.status(HttpStatus.CREATED).body(watchlist);
    }

    @DeleteMapping("/watchlist/{symbol}")
    @Operation(summary = "Remove from watchlist", description = "Remove a stock from user's watchlist")
    public ResponseEntity<Void> removeFromWatchlist(@PathVariable String symbol) {
        Long userId = authenticationHelper.getCurrentUserId();
        stockManagementService.removeFromWatchlist(userId, symbol);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/watchlist")
    @Operation(summary = "Get user watchlist", description = "Retrieve user's stock watchlist with current prices")
    public ResponseEntity<WatchlistResponse> getUserWatchlist() {
        Long userId = authenticationHelper.getCurrentUserId();
        return ResponseEntity.ok(stockManagementService.getUserWatchlist(userId));
    }

    // ==================== Price Alerts ====================

    @PostMapping("/alerts")
    @Operation(summary = "Create price alert", description = "Create a price alert for a stock")
    public ResponseEntity<PriceAlert> createAlert(@Valid @RequestBody CreateAlertRequest request) {
        Long userId = authenticationHelper.getCurrentUserId();
        PriceAlert alert = stockManagementService.createAlert(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(alert);
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get user alerts", description = "Retrieve all alerts for the user")
    public ResponseEntity<List<PriceAlert>> getUserAlerts() {
        Long userId = authenticationHelper.getCurrentUserId();
        return ResponseEntity.ok(stockManagementService.getUserAlerts(userId));
    }

    @DeleteMapping("/alerts/{alertId}")
    @Operation(summary = "Delete alert", description = "Delete a price alert")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long alertId) {
        Long userId = authenticationHelper.getCurrentUserId();
        stockManagementService.deleteAlert(alertId, userId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Corporate Actions ====================

    @GetMapping("/{symbol}/corporate-actions")
    @Operation(summary = "Get corporate actions", description = "Retrieve corporate actions for a stock")
    public ResponseEntity<CorporateActionsResponse> getCorporateActions(@PathVariable String symbol) {
        return ResponseEntity.ok(stockManagementService.getCorporateActions(symbol));
    }

    @PostMapping("/{symbol}/corporate-actions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Save corporate action", description = "Admin-only endpoint to save corporate action")
    public ResponseEntity<CorporateAction> saveCorporateAction(@PathVariable String symbol, @Valid @RequestBody CorporateAction action) {
        action.setSymbol(symbol.toUpperCase());
        CorporateAction saved = stockManagementService.saveCorporateAction(action);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/corporate-actions/upcoming")
    @Operation(summary = "Get upcoming corporate actions", description = "Retrieve all upcoming corporate actions")
    public ResponseEntity<List<CorporateAction>> getUpcomingActions() {
        return ResponseEntity.ok(stockManagementService.getUpcomingActions());
    }
}
