package com.investments.stocks.service;

import com.investments.stocks.data.StockResponse;

public interface StockReadPlatformService {
    StockResponse getStockBySymbol(String symbol);
}
