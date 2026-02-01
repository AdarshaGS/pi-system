package com.investments.stocks.thirdParty.providers.AlphaVantage.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response model for Alpha Vantage GLOBAL_QUOTE endpoint
 * Real-time stock price data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlphaVantageGlobalQuote {
    
    @JsonProperty("Global Quote")
    private GlobalQuote globalQuote;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GlobalQuote {
        @JsonProperty("01. symbol")
        private String symbol;
        
        @JsonProperty("02. open")
        private String open;
        
        @JsonProperty("03. high")
        private String high;
        
        @JsonProperty("04. low")
        private String low;
        
        @JsonProperty("05. price")
        private String price;
        
        @JsonProperty("06. volume")
        private String volume;
        
        @JsonProperty("07. latest trading day")
        private String latestTradingDay;
        
        @JsonProperty("08. previous close")
        private String previousClose;
        
        @JsonProperty("09. change")
        private String change;
        
        @JsonProperty("10. change percent")
        private String changePercent;
    }
    
    /**
     * Get price as Double, handling null/empty values
     */
    public Double getPriceAsDouble() {
        if (globalQuote == null || globalQuote.getPrice() == null || globalQuote.getPrice().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(globalQuote.getPrice());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Check if response is valid
     */
    public boolean isValid() {
        return globalQuote != null 
            && globalQuote.getSymbol() != null 
            && !globalQuote.getSymbol().isEmpty()
            && globalQuote.getPrice() != null 
            && !globalQuote.getPrice().isEmpty();
    }
}
