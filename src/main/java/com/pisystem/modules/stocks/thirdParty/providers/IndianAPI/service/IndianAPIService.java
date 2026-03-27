package com.pisystem.modules.stocks.thirdParty.providers.IndianAPI.service;

import com.pisystem.modules.stocks.thirdParty.ThirdPartyResponse;

public interface IndianAPIService {
    ThirdPartyResponse fetchStockData(String symbol);
}
