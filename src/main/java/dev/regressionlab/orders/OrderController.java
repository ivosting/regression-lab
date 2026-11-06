package dev.regressionlab.orders;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping("/orders")
    public List<Order> list(@RequestParam(required = false) String status) {
        List<Order> orders = service.listRecentOrders();
        if (status == null || status.isBlank()) {
            return orders;
        }
        return orders.stream().filter(o -> o.status().equalsIgnoreCase(status.trim())).toList();
    }
}
