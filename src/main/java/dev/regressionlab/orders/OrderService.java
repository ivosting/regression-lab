package dev.regressionlab.orders;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository orders;
    private final CustomerRepository customers;

    public OrderService(OrderRepository orders, CustomerRepository customers) {
        this.orders = orders;
        this.customers = customers;
    }

    public List<Order> listRecentOrders() {
        List<OrderRepository.OrderRow> rows = orders.findRecent();
        List<Long> customerIds = rows.stream().map(OrderRepository.OrderRow::customerId).distinct().toList();
        Map<Long, String> names = customers.findNamesByIds(customerIds);
        return rows.stream()
                .map(r -> new Order(r.id(), r.customerId(), names.get(r.customerId()), r.status(), r.total()))
                .toList();
    }
}
