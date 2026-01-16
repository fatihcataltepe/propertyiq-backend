package com.propertyiq.portfolio.mortgage.model;

public enum ProductType {
    FIXED("Fixed Rate", "Interest rate fixed for the entire term"),
    VARIABLE("Variable Rate", "Rate changes with the lender's standard variable rate"),
    TRACKER("Tracker Rate", "Rate tracks the Bank of England base rate plus a margin"),
    OFFSET("Offset Mortgage", "Interest calculated against net balance after savings offset"),
    SVR("Standard Variable Rate", "Lender's standard variable interest rate product");

    private final String displayName;
    private final String description;

    ProductType(String displayName, String description) {
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
