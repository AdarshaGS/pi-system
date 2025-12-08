package com.stocks.thirdParty.providers.IndianAPI;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.stocks.thirdParty.StockDataProvider;
import com.stocks.thirdParty.ThirdPartyResponse;
import com.stocks.thirdParty.providers.IndianAPI.service.IndianAPIService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Primary
@Slf4j
@RequiredArgsConstructor
public class IndianApiProvider implements StockDataProvider {

    private final IndianAPIService indianAPIService;

    @Override
    public ThirdPartyResponse fetchStockData(String symbol) {
        return indianAPIService.fetchStockData(symbol);
    }

    @Override
    public String getProviderName() {
        return "IndianAPI";
    }
}
