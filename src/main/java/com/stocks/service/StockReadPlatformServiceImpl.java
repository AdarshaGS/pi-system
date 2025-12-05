package com.stocks.service;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.stocks.data.Stock;
import com.stocks.exception.SymbolNotFoundException;
import com.stocks.repo.StockRepository;
import com.stocks.thirdParty.ThirdPartyResponse;
import com.stocks.thirdParty.service.IndianAPIService;

@Service
public class StockReadPlatformServiceImpl implements StockReadPlatformService {

    final JdbcTemplate jdbcTemplate;
    final IndianAPIService indianAPIService;
    final StockRepository stockRepository;

    public StockReadPlatformServiceImpl(final JdbcTemplate jdbcTemplate, final IndianAPIService indianAPIService,
            final StockRepository stockRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.indianAPIService = indianAPIService;
        this.stockRepository = stockRepository;
    }

    @Override
    public Stock getStockBySymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return null;
        }
        Stock stock = null;
        symbol = symbol.toUpperCase();
        String sql = "SELECT id, symbol, name, price FROM stocks WHERE symbol = ?";

        try {
            stock = jdbcTemplate.queryForObject(
                    sql,
                    new BeanPropertyRowMapper<>(Stock.class),
                    symbol);
        } catch (EmptyResultDataAccessException ex) {
            // Not found in DB → call third-party
            ThirdPartyResponse response = this.indianAPIService.fetchStockData(symbol);
            if (response == null) {
                throw new SymbolNotFoundException("Symbol not found in third-party API: ");
            }
            stock = Stock.builder()
                    .symbol(symbol)
                    .companyName(response.getCompanyName())
                    // .price(response.getPrice())
                    .build();
            stock = this.stockRepository.save(stock);
        }
        return stock;
    }
}
