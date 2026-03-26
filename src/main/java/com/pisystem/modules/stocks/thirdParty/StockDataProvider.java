package com.investments.stocks.thirdParty;

public interface StockDataProvider {

    ThirdPartyResponse fetchStockData(String symbol);

    String getProviderName();
}
