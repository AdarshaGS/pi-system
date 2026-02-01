package com.investments.stocks.thirdParty.factory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.investments.stocks.thirdParty.StockDataProvider;
import com.investments.stocks.thirdParty.ThirdPartyResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StockDataProviderFactory {

    private final Map<String, StockDataProvider> providerMap;

    public StockDataProviderFactory(List<StockDataProvider> providers) {
        this.providerMap = providers.stream()
                .collect(Collectors.toMap(StockDataProvider::getProviderName, Function.identity()));
    }

    /**
     * Fetch stock data with automatic fallback and caching.
     * 
     * Caching Strategy:
     * - Cache Name: "stockPrices"
     * - Cache Key: Stock symbol (e.g., "RELIANCE", "TCS")
     * - TTL: 5 minutes (configured in RedisCacheConfig)
     * - Cache on successful responses only (null values disabled)
     * 
     * This method implements a fallback chain:
     * 1. Try AlphaVantage (primary)
     * 2. If fails, try IndianAPI (secondary)
     * 3. If both fail, throw exception
     * 
     * Cached responses skip external API calls entirely, respecting rate limits.
     */
    @Cacheable(value = "stockPrices", key = "#symbol", cacheManager = "stockCacheManager")
    public ThirdPartyResponse fetchStockDataWithRetry(String symbol) {
        StockDataProvider primary = providerMap.get("AlphaVantage");
        StockDataProvider secondary = providerMap.get("IndianAPI");

        try {
            log.info("Attempting to fetch data for {} using Primary: AlphaVantage", symbol);
            return primary.fetchStockData(symbol);
        } catch (Exception e) {
            log.warn("Primary Provider (AlphaVantage) failed for {}: {}. Switching to Secondary: IndianAPI.", symbol,
                    e.getMessage());
            if (secondary != null) {
                try {
                    log.info("Attempting to fetch data for {} using Secondary: IndianAPI", symbol);
                    return secondary.fetchStockData(symbol);
                } catch (Exception ex) {
                    log.error("Secondary Provider (IndianAPI) also failed for {}: {}", symbol, ex.getMessage());
                    throw ex;
                }
            } else {
                log.error("Secondary Provider not configured.");
                throw e;
            }
        }
    }
}
