package dev.regressionlab.orders;

import java.math.BigDecimal;

public record Order(long id, long customerId, String customerName, String status, BigDecimal total) {
}
