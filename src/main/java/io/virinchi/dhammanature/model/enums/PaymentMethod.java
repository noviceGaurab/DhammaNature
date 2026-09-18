package io.virinchi.dhammanature.model.enums;

/** How a marketplace order or paid event booking is settled. */
public enum PaymentMethod {
    ESEWA("eSewa"),
    CARD("Debit / Credit Card"),
    REDEEMED_POINTS("Reward Points"),
    CASH_ON_DELIVERY("Cash on Delivery");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}