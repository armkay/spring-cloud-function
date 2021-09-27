package example.service;


import example.dto.Order;

import java.util.List;

public interface OrderServiceAbstract {

	List<Order> getAllOrders();

	Order getOrderByOrderType(String orderName);
}
