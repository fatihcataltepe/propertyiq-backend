package com.propertyiq.portfolio.mortgage.model;

public enum PaymentStatus {
    SCHEDULED("Scheduled"),
    PAID("Paid"),
    MISSED("Missed"),
    OVERPAID("Overpaid");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
