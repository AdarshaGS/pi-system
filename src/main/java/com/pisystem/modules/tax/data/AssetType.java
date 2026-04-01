package com.pisystem.modules.tax.data;

public enum AssetType {
    LISTED_EQUITY("Listed Equity Shares"),
    UNLISTED_EQUITY("Unlisted Equity Shares"),
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
