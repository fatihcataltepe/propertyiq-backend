package com.propertyiq.portfolio.mortgage.model;

public enum MortgageType {
    REPAYMENT("Repayment Mortgage", "Interest and principal are paid each month"),
    INTEREST_ONLY("Interest-Only Mortgage", "Only interest is paid; principal due at end of term");

    private final String displayName;
    private final String description;

    MortgageType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
