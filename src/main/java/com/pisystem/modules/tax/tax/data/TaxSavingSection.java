package com.tax.data;

public enum TaxSavingSection {
    SECTION_80C("80C", "Life Insurance, PPF, EPF, ELSS, etc.", 150000),
    SECTION_80D("80D", "Health Insurance Premium", 25000),
    SECTION_80D_SENIOR("80D (Senior Citizen)", "Health Insurance Premium for Senior Citizens", 50000),
    SECTION_80E("80E", "Education Loan Interest", 0), // No limit
    SECTION_80G("80G", "Charitable Donations", 0), // No limit
    SECTION_80TTA("80TTA", "Savings Account Interest", 10000),
    SECTION_80TTB("80TTB (Senior Citizen)", "Interest on Deposits for Senior Citizens", 50000),
    SECTION_24B("24B", "Home Loan Interest", 200000),
    SECTION_80CCD_1B("80CCD(1B)", "NPS Additional Deduction", 50000),
    SECTION_80EEA("80EEA", "Home Loan Interest (First-Time Buyer)", 150000),
    SECTION_80EEB("80EEB", "Electric Vehicle Loan Interest", 150000);

    private final String code;
    private final String description;
    private final long maxLimit;

    TaxSavingSection(String code, String description, long maxLimit) {
        this.code = code;
        this.description = description;
        this.maxLimit = maxLimit;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public long getMaxLimit() {
        return maxLimit;
    }
}
