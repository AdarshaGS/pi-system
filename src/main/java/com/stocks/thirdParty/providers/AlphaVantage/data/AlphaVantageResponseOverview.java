package com.stocks.thirdParty.providers.AlphaVantage.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AlphaVantageResponseOverview {

    @JsonProperty("Symbol")
    private String symbol;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Description")
    private String description;
    @JsonProperty("Industry")
    private String industry;
    @JsonProperty("MarketCapitalization")
    private String marketCapitalization;
}
