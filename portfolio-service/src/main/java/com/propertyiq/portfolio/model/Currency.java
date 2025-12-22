package com.propertyiq.portfolio.model;

public enum Currency {
    GBP("British Pound", "£"),
    USD("US Dollar", "$"),
    EUR("Euro", "€");

    private final String displayName;
    private final String symbol;

    Currency(String displayName, String symbol) {
        this.displayName = displayName;
        this.symbol = symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }
}
