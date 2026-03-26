package com.tax.data;

public enum CapitalGainType {
    SHORT_TERM("Short Term Capital Gains"),
    LONG_TERM("Long Term Capital Gains");

    private final String displayName;

    CapitalGainType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
