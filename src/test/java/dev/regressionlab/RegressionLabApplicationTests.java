package dev.regressionlab;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import dev.regressionlab.orders.Order;
import dev.regressionlab.orders.OrderService;

@SpringBootTest(properties = {"lab.simulated-latency.enabled=false", "LOG_FILE=build/test-logs/test.json"})
class RegressionLabApplicationTests {

    @Autowired
    OrderService service;

    @Test
    void listsTwentyOrdersWithCustomerNames() {
        List<Order> orders = service.listRecentOrders();
        assertThat(orders).hasSize(20);
        assertThat(orders).allSatisfy(o -> assertThat(o.customerName()).isNotBlank());
    }
}
