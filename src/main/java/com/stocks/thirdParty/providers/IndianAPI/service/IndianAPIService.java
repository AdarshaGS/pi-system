package com.stocks.thirdParty.providers.IndianAPI.service;

import com.stocks.thirdParty.ThirdPartyResponse;

public interface IndianAPIService {
    ThirdPartyResponse fetchStockData(String symbol);
}
