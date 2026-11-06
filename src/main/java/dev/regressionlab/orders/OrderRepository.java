package dev.regressionlab.orders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

/**
 * In-memory stand-in for the orders table. {@link SimulatedLatency} models the
 * round-trip cost of a real database query so that the endpoint has realistic timing.
 */
@Repository
public class OrderRepository {

    static final int ORDER_COUNT = 20;
    private static final String[] STATUSES = {"NEW", "PAID", "SHIPPED", "DELIVERED"};

    private final SimulatedLatency latency;
    private final List<OrderRow> rows = new ArrayList<>();

    public OrderRepository(SimulatedLatency latency) {
        this.latency = latency;
        for (int i = 1; i <= ORDER_COUNT; i++) {
            rows.add(new OrderRow(i, 100 + (i % 8), STATUSES[i % STATUSES.length],
                    BigDecimal.valueOf(1000L + i * 137L, 2)));
        }
    }

    /** Single query returning all recent orders (~35 ms). */
    public List<OrderRow> findRecent() {
        latency.query(35);
        return List.copyOf(rows);
    }

    public record OrderRow(long id, long customerId, String status, BigDecimal total) {
    }
}
