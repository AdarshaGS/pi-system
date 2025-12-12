package com.investments.stocks.thirdParty.factory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public ThirdPartyResponse fetchStockDataWithRetry(String symbol) {
        StockDataProvider primary = providerMap.get("AlphaVantage");
        StockDataProvider secondary = providerMap.get("IndianAPI");

        try {
            log.info("Attempting to fetch data for {} using Primary: AlphaVantage", symbol);
            return primary.fetchStockData(symbol);
        } catch (Exception e) {
            log.warn("Primary Provider (AlphaVantage) failed for {}: {}. Switching to Secondary: AlphaVantage.", symbol,
                    e.getMessage());
            if (secondary != null) {
                try {
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
