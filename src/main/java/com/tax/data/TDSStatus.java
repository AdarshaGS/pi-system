package com.tax.data;

public enum TDSStatus {
    PENDING("Pending Verification"),
    VERIFIED("Verified"),
    CLAIMED("Claimed in ITR"),
    MISMATCH("Mismatch with 26AS");

    private final String displayName;

    TDSStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
