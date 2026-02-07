package com.investments.stocks.controller;

import com.investments.stocks.dto.StockPriceUpdate;
import com.investments.stocks.service.StockPriceWebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * WebSocket controller for broadcasting real-time stock price updates.
 * Clients can subscribe to /topic/stock-prices to receive live updates.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class StockPriceWebSocketController {

    private final StockPriceWebSocketService stockPriceService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Scheduled task to broadcast stock prices every 30 seconds.
     * Adjust the rate based on your external API limits and user needs.
     */
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void broadcastStockPrices() {
        try {
            List<StockPriceUpdate> updates = stockPriceService.fetchLatestPrices();
            
            if (!updates.isEmpty()) {
                // Broadcast to all subscribers on /topic/stock-prices
                messagingTemplate.convertAndSend("/topic/stock-prices", updates);
                log.debug("Broadcasted {} stock price updates", updates.size());
            }
        } catch (Exception e) {
            log.error("Error broadcasting stock prices", e);
        }
    }

    /**
     * Handle client requests for specific stock prices.
     * Client sends to /app/stock-price/{symbol}
     * Response is sent to /topic/stock-price/{symbol}
     */
    @MessageMapping("/stock-price/{symbol}")
    @SendTo("/topic/stock-price/{symbol}")
    public StockPriceUpdate getStockPrice(@org.springframework.messaging.handler.annotation.DestinationVariable String symbol) {
        log.debug("Client requested price for symbol: {}", symbol);
        return stockPriceService.fetchSingleStockPrice(symbol);
    }

    /**
     * REST endpoint to manually trigger a broadcast.
     * Useful for testing and on-demand updates.
     */
    @PostMapping("/api/v1/stocks/price/broadcast")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> triggerBroadcast() {
        try {
            List<StockPriceUpdate> updates = stockPriceService.fetchLatestPrices();
            messagingTemplate.convertAndSend("/topic/stock-prices", updates);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Broadcasted price updates",
                "count", updates.size()
            ));
        } catch (Exception e) {
            log.error("Error in manual broadcast", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    /**
     * REST endpoint to force refresh prices for a specific symbol.
     */
    @PostMapping("/api/v1/stocks/price/refresh/{symbol}")
    @ResponseBody
    public ResponseEntity<StockPriceUpdate> refreshStockPrice(@PathVariable String symbol) {
        try {
            StockPriceUpdate update = stockPriceService.refreshAndBroadcastSingleStock(symbol);
            return ResponseEntity.ok(update);
        } catch (Exception e) {
            log.error("Error refreshing price for {}", symbol, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
