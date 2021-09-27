package example.service;

import example.dto.Order;
import example.repository.OrderRepositoryAbstract;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderService implements OrderServiceAbstract {

	private final OrderRepositoryAbstract orderRepository;


	@Override
	public List<Order> getAllOrders() {
		return orderRepository.getAllOrders();
	}

	@Override
	public Order getOrderByOrderType(String orderName) {
		return orderRepository.getOrderByType(orderName);
	}
}
