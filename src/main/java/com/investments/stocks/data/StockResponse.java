package com.investments.stocks.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class StockResponse {
    private String companyName;
    private String description;
    private double nsePrice;
    private double bsePrice;
    private String sector;
    private double marketCap;

    public StockResponse() {
    }

}
