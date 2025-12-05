package com.stocks.thirdParty.service;

import com.stocks.thirdParty.ThirdPartyResponse;

public interface IndianAPIService {
    ThirdPartyResponse fetchStockData(String symbol);
}
