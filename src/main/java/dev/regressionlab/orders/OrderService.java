package dev.regressionlab.orders;

import java.util.List;

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
        return orders.findRecent().stream()
                .map(this::toOrder)
                .toList();
    }

    private Order toOrder(OrderRepository.OrderRow row) {
        CustomerRepository.CustomerProfile customer = customers.findProfileById(row.customerId());
        return new Order(row.id(), row.customerId(), customer.name(), customer.loyaltyTier(), row.status(), row.total());
    }
}
