package com.investments.stocks.thirdParty.providers.IndianAPI.service;

import com.investments.stocks.thirdParty.ThirdPartyResponse;

public interface IndianAPIService {
    ThirdPartyResponse fetchStockData(String symbol);
}
