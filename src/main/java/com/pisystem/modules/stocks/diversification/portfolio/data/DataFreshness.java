package com.pisystem.modules.stocks.diversification.portfolio.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataFreshness {
    private String priceLastUpdatedAt;
    private boolean isStale;
}
