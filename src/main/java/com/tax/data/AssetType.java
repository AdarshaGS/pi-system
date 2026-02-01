package com.tax.data;

public enum AssetType {
    LISTED_EQUITY("Listed Equity Shares"),
    UNLISTED_EQUITY("Unlisted Equity Shares"),
    EQUITY_MUTUAL_FUND("Equity Mutual Funds"),
    DEBT_MUTUAL_FUND("Debt Mutual Funds"),
    ETF("Exchange Traded Funds"),
    BONDS("Bonds/Debentures"),
    GOLD("Gold/Gold Bonds"),
    REAL_ESTATE("Real Estate"),
    OTHER("Other Assets");

    private final String displayName;

    AssetType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
