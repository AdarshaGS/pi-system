package com.tax.data;

public enum TaxRegime {
    OLD("Old Tax Regime"),
    NEW("New Tax Regime");

    private final String displayName;

    TaxRegime(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
