package com.stocks.service;

import com.stocks.data.Stock;

public interface StockReadPlatformService {
    Stock getStockBySymbol(String symbol);
}
