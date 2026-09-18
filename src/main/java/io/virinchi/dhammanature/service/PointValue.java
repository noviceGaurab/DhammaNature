package io.virinchi.dhammanature.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Defines what one reward point is worth in money, so a user can pay for
 * marketplace orders, paid events or the 24/7 volunteer donation drive using
 * points they have already earned.
 */
public final class PointValue {

    /** One reward point covers this much of an order/bookable amount. */
    public static final BigDecimal VALUE_PER_POINT = new BigDecimal("0.50");

    private PointValue() {
    }

    /** Number of points needed to cover the given money amount, rounded up. */
    public static int pointsFor(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            return 0;
        }
        return amount.divide(VALUE_PER_POINT, 0, RoundingMode.CEILING).intValueExact();
    }

    /** The money value of the given number of reward points. */
    public static BigDecimal moneyValue(int points) {
        return VALUE_PER_POINT.multiply(BigDecimal.valueOf(points));
    }
}