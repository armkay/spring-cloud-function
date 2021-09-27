package example.repository;

import example.dto.Order;
import org.springframework.http.StreamingHttpOutputMessage;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class OrderRepositoryAbstract {

	public List<Order> getAllOrders(){
		return Stream.of(
			Order.builder()
			.orderName("Harry Potter 1")
			.orderNumber("1")
			.orderType("Book")
			.build(),
			Order.builder()
				.orderName("Harry Potter 2")
				.orderNumber("2")
				.orderType("Book")
				.build(),
			Order.builder()
				.orderName("Die Hard")
				.orderNumber("3")
				.orderType("Movie")
				.build()).collect(Collectors.toList());
	}

	public Order getOrderByType(String orderType){
		List<Order> listOfOrders = new ArrayList<>();
		listOfOrders.add(
			Order.builder()
				.orderName("Harry Potter 1")
				.orderNumber("1")
				.orderType("Book")
				.build()
		);
		listOfOrders.add(
			Order.builder()
				.orderName("Harry Potter 2")
				.orderNumber("2")
				.orderType("Book")
				.build()
		);
		listOfOrders.add(Order.builder()
			.orderName("Die Hard")
			.orderNumber("3")
			.orderType("Movie")
			.build());

	return listOfOrders.stream().filter(order -> order.getOrderType().equalsIgnoreCase(orderType)).findFirst().orElse(null);
	}
}
