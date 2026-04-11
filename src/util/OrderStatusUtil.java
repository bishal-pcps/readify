package util;

public final class OrderStatusUtil {
    public static final int PENDING = 1;
    public static final int SHIPPED = 2;
    public static final int CANCELLED = 3;
    public static final int COMPLETED = 4;

    private OrderStatusUtil() {
    }

    public static String label(int statusId) {
        switch (statusId) {
            case PENDING: return "Pending";
            case SHIPPED: return "Shipped";
            case CANCELLED: return "Cancelled";
            case COMPLETED: return "Completed";
            default: return "Unknown";
        }
    }

    public static String color(int statusId) {
        switch (statusId) {
            case PENDING: return "#E6B422";
            case SHIPPED: return "#5A6F8C";
            case CANCELLED: return "#B14A3A";
            case COMPLETED: return "#7A9E6B";
            default: return "#A8978A";
        }
    }

    public static String emoji(int statusId) {
        switch (statusId) {
            case PENDING: return "⏳";
            case SHIPPED: return "🚚";
            case CANCELLED: return "✖";
            case COMPLETED: return "✅";
            default: return "•";
        }
    }

    public static boolean isPending(int statusId) {
        return statusId == PENDING;
    }

    public static boolean isCancelled(int statusId) {
        return statusId == CANCELLED;
    }
}
