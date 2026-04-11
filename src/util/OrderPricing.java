package util;

public final class OrderPricing {
    public static final double SHIPPING_FEE = 5.99;

    private OrderPricing() {
    }

    public static double orderTotal(double subtotal) {
        return subtotal + SHIPPING_FEE;
    }
}
