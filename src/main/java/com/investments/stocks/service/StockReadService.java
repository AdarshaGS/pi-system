package com.investments.stocks.service;

import com.investments.stocks.data.StockResponse;

public interface StockReadService {
    StockResponse getStockBySymbol(String symbol);
}
