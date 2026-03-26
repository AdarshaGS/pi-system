package com.investments.stocks.service;

import com.investments.stocks.data.Stock;
import com.investments.stocks.data.StockPrice;
import com.investments.stocks.dto.StockPriceUpdate;
import com.investments.stocks.repo.StockPriceRepository;
import com.investments.stocks.repo.StockRepository;
import com.investments.stocks.thirdParty.ThirdPartyResponse;
import com.investments.stocks.thirdParty.providers.IndianAPI.service.IndianAPIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for fetching and broadcasting real-time stock prices via WebSocket.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockPriceWebSocketService {

    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final IndianAPIService indianAPIService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Fetch latest prices for all stocks in the database.
     * This method is called by the scheduled broadcaster.
     */
    @Transactional(readOnly = true)
    public List<StockPriceUpdate> fetchLatestPrices() {
        List<StockPriceUpdate> updates = new ArrayList<>();
        List<Stock> allStocks = stockRepository.findAll();

        for (Stock stock : allStocks) {
            try {
                StockPriceUpdate update = fetchSingleStockPrice(stock.getSymbol());
                if (update != null && update.getCurrentPrice() != null) {
                    updates.add(update);
                    
                    // Update the stock entity with latest price
                    stock.setPrice(update.getCurrentPrice().doubleValue());
                    stockRepository.save(stock);
                }
            } catch (Exception e) {
                log.warn("Failed to fetch price for {}: {}", stock.getSymbol(), e.getMessage());
            }
        }

        return updates;
    }

    /**
     * Fetch price for a single stock symbol.
     */
    public StockPriceUpdate fetchSingleStockPrice(String symbol) {
        try {
            // Fetch from external API
            ThirdPartyResponse response = indianAPIService.fetchStockData(symbol);

            if (response != null && response.getCurrentPrice() != null) {
                Double nsePrice = response.getCurrentPrice().getNSE();
                Double bsePrice = response.getCurrentPrice().getBSE();
                
                BigDecimal currentPrice = null;
                if (nsePrice != null) {
                    currentPrice = BigDecimal.valueOf(nsePrice);
                } else if (bsePrice != null) {
                    currentPrice = BigDecimal.valueOf(bsePrice);
                }

                if (currentPrice == null) {
                    log.warn("No price data available for {}", symbol);
                    return null;
                }

                // Get previous close from stock_prices table
                BigDecimal previousClose = getPreviousClose(symbol);

                // Build the update DTO
                StockPriceUpdate update = StockPriceUpdate.builder()
                        .symbol(symbol)
                        .currentPrice(currentPrice)
                        .previousClose(previousClose)
                        .timestamp(LocalDateTime.now())
                        .build();

                // Additional data like dayHigh, dayLow not available in current API response
                
                // Calculate change and change percentage
                update.calculateChange();

                // Save to stock_prices table for historical tracking
                saveStockPriceHistory(symbol, response);

                return update;
            }
        } catch (Exception e) {
            log.error("Error fetching price for {}", symbol, e);
        }

        return null;
    }

    /**
     * Refresh a single stock and broadcast immediately.
     */
    public StockPriceUpdate refreshAndBroadcastSingleStock(String symbol) {
        StockPriceUpdate update = fetchSingleStockPrice(symbol);
        
        if (update != null) {
            // Broadcast to subscribers of this specific symbol
            messagingTemplate.convertAndSend("/topic/stock-price/" + symbol, update);
            
            // Also broadcast to general stock prices topic
            messagingTemplate.convertAndSend("/topic/stock-prices", List.of(update));
            
            log.info("Broadcasted price update for {}: {}", symbol, update.getCurrentPrice());
        }
        
        return update;
    }

    /**
     * Get the previous close price from stock_prices table.
     */
    private BigDecimal getPreviousClose(String symbol) {
        try {
            // Get the most recent stock price entry (yesterday's close)
            Optional<StockPrice> latestPrice = stockPriceRepository
                    .findTopBySymbolOrderByPriceDateDesc(symbol);
            
            return latestPrice.map(StockPrice::getClosePrice)
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Could not fetch previous close for {}", symbol);
            return null;
        }
    }

    /**
     * Save stock price data to stock_prices table for historical tracking.
     */
    private void saveStockPriceHistory(String symbol, ThirdPartyResponse response) {
        try {
            LocalDate today = LocalDate.now();
            
            // Check if we already have an entry for today
            Optional<StockPrice> existingPrice = stockPriceRepository
                    .findBySymbolAndPriceDate(symbol, today);

            StockPrice stockPrice;
            if (existingPrice.isPresent()) {
                // Update existing record
                stockPrice = existingPrice.get();
            } else {
                // Create new record
                stockPrice = new StockPrice();
                stockPrice.setSymbol(symbol);
                stockPrice.setPriceDate(today);
            }

            // Update prices (convert Double to BigDecimal)
            Double nse = response.getCurrentPrice().getNSE();
            Double bse = response.getCurrentPrice().getBSE();
            
            if (nse != null) {
                stockPrice.setClosePrice(BigDecimal.valueOf(nse));
            } else if (bse != null) {
                stockPrice.setClosePrice(BigDecimal.valueOf(bse));
            }

            // DayHigh, DayLow, Open not available in current API response
            // These fields would need to be added to the ThirdPartyResponse.currentPrice class

            // Save to database
            stockPriceRepository.save(stockPrice);
            
        } catch (Exception e) {
            log.warn("Failed to save price history for {}: {}", symbol, e.getMessage());
        }
    }
}
