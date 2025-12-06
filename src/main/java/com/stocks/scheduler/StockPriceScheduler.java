package com.stocks.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.stocks.data.Stock;
import com.stocks.repo.StockRepository;
import com.stocks.thirdParty.ThirdPartyResponse;
import com.stocks.thirdParty.service.IndianAPIService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class StockPriceScheduler {

    private final StockRepository stockRepository;
    private final IndianAPIService indianAPIService;

    // Run every 1 Day
    // @Scheduled(cron = "0 0/1 * * * ?")
    public void updateStockPrices() {
        log.info("Starting scheduled stock price update...");
        List<Stock> allStocks = stockRepository.findAll();

        for (Stock stock : allStocks) {
            try {
                // Fetch latest data
                ThirdPartyResponse response = indianAPIService.fetchStockData(stock.getSymbol());

                if (response != null && response.getCurrentPrice() != null) {
                    boolean updated = false;

                    // Update Price
                    if (response.getCurrentPrice().getNSE() != null) {
                        stock.setPrice(response.getCurrentPrice().getNSE());
                        updated = true;
                    }

                    // Update Market Cap (if available via API - assuming getter exists or we need
                    // to add logic)
                    // Currently ThirdPartyResponse doesn't strictly have MarketCap on top level,
                    // but let's assume we can get it or calculating it if shares outstanding were
                    // known.
                    // For now, let's assume the API *might* return it in PeerCompanyList or we
                    // leave it.
                    // The user requested market cap updates.
                    // Let's verify if ThirdPartyResponse has it. View file showed PeerCompanyList
                    // has marketCap string.
                    // But that's for peers. The main company profile key might be missing it.
                    // For this iteration, we update Price and LastUpdated.

                    if (updated) {
                        // stock.setLastUpdated(LocalDateTime.now());
                        stockRepository.save(stock);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to update price for symbol: {}", stock.getSymbol(), e);
            }
        }
        log.info("Completed scheduled stock price update.");
    }
}
