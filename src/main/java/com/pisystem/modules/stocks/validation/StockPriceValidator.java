package com.investments.stocks.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Validator for stock price data from third-party APIs
 * Ensures data quality and prevents invalid data from entering the system
 */
@Component
@Slf4j
public class StockPriceValidator {
    
    private static final double MIN_PRICE = 0.01;
    private static final double MAX_PRICE = 1000000.0; // 10 lakh max price
    private static final int MAX_DATA_AGE_DAYS = 7; // Warn if data is older than 7 days
    
    /**
     * Validate stock price is within reasonable range
     * 
     * @param price Stock price to validate
     * @param symbol Stock symbol for logging
     * @return true if valid, false otherwise
     */
    public boolean isValidPrice(Double price, String symbol) {
        if (price == null) {
            log.warn("Price is null for symbol: {}", symbol);
            return false;
        }
        
        if (price <= 0) {
            log.error("Invalid price (<=0) for symbol {}: {}", symbol, price);
            return false;
        }
        
        if (price < MIN_PRICE) {
            log.warn("Price too low for symbol {}: {}. Minimum: {}", symbol, price, MIN_PRICE);
            return false;
        }
        
        if (price > MAX_PRICE) {
            log.error("Price too high for symbol {}: {}. Maximum: {}", symbol, price, MAX_PRICE);
            return false;
        }
        
        return true;
    }
    
    /**
     * Validate symbol format (Indian stocks)
     * 
     * @param symbol Stock symbol
     * @return true if valid format
     */
    public boolean isValidSymbol(String symbol) {
        if (symbol == null || symbol.isEmpty()) {
            log.error("Symbol is null or empty");
            return false;
        }
        
        // Indian stock symbols: RELIANCE, TCS, INFY, etc. or RELIANCE.BSE
        // Allow alphanumeric and optional .BSE/.NS suffix
        if (!symbol.matches("^[A-Z0-9]+(\\.BSE|\\.NS)?$")) {
            log.error("Invalid symbol format: {}. Expected format: SYMBOL or SYMBOL.BSE", symbol);
            return false;
        }
        
        return true;
    }
    
    /**
     * Validate trading date is recent (not stale data)
     * 
     * @param dateString Date string in format yyyy-MM-dd
     * @param symbol Stock symbol for logging
     * @return true if data is fresh
     */
    public boolean isFreshData(String dateString, String symbol) {
        if (dateString == null || dateString.isEmpty()) {
            log.warn("Trading date is null/empty for symbol: {}", symbol);
            return false;
        }
        
        try {
            LocalDate tradingDate = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate today = LocalDate.now();
            long daysDifference = java.time.temporal.ChronoUnit.DAYS.between(tradingDate, today);
            
            if (daysDifference > MAX_DATA_AGE_DAYS) {
                log.warn("Stale data detected for symbol {}: Trading date is {} ({} days old)", 
                    symbol, dateString, daysDifference);
                return false;
            }
            
            if (daysDifference < 0) {
                log.error("Future date detected for symbol {}: {}", symbol, dateString);
                return false;
            }
            
            return true;
            
        } catch (DateTimeParseException e) {
            log.error("Invalid date format for symbol {}: {}. Expected: yyyy-MM-dd", symbol, dateString);
            return false;
        }
    }
    
    /**
     * Comprehensive validation of all stock data fields
     * 
     * @param symbol Stock symbol
     * @param price Current price
     * @param tradingDate Latest trading date
     * @return true if all validations pass
     */
    public boolean validateStockData(String symbol, Double price, String tradingDate) {
        boolean symbolValid = isValidSymbol(symbol);
        boolean priceValid = isValidPrice(price, symbol);
        boolean dataFresh = isFreshData(tradingDate, symbol);
        
        boolean allValid = symbolValid && priceValid && dataFresh;
        
        if (!allValid) {
            log.error("Stock data validation failed for symbol {}: symbol={}, price={}, dataFresh={}", 
                symbol, symbolValid, priceValid, dataFresh);
        }
        
        return allValid;
    }
    
    /**
     * Validate price change percentage is reasonable
     * Circuit breaker: NSE/BSE has +/-20% circuit limit for most stocks
     * 
     * @param changePercent Change percentage as string (e.g., "5.25%")
     * @param symbol Stock symbol
     * @return true if change is within limits or data is unavailable
     */
    public boolean isReasonableChange(String changePercent, String symbol) {
        if (changePercent == null || changePercent.isEmpty()) {
            return true; // No change data available, allow by default
        }
        
        try {
            // Remove % sign and parse
            String cleanPercent = changePercent.replace("%", "").trim();
            double change = Double.parseDouble(cleanPercent);
            
            if (Math.abs(change) > 20.0) {
                log.warn("Large price change detected for symbol {}: {}%. Possible circuit breaker hit or data error.", 
                    symbol, change);
                return false;
            }
            
            return true;
            
        } catch (NumberFormatException e) {
            log.warn("Invalid change percent format for symbol {}: {}. Allowing by default.", symbol, changePercent);
            return true; // Invalid format, but don't block request - allow by default
        }
    }
}
