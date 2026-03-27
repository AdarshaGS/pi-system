package com.pisystem.modules.stocks.service;

import com.pisystem.modules.stocks.data.StockResponse;

public interface StockReadService {
    StockResponse getStockBySymbol(String symbol);
}
