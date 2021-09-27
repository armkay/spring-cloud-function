package example;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import example.service.OrderService;
import example.service.OrderServiceAbstract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FunctionConfiguration {


	@Autowired
	public OrderServiceAbstract orderDao;
	/*
	 * You need this main method (empty) or explicit <start-class>example.FunctionConfiguration</start-class>
	 * in the POM to ensure boot plug-in makes the correct entry
	 */
	public static void main(String[] args) {
		// empty unless using Custom runtime at which point it should include
		// SpringApplication.run(FunctionConfiguration.class, args);
	}

	@Bean
	public Function<String, Object> getOrderByType() {
		return value -> {
			if (value.equals("exception")) {
				throw new RuntimeException("Intentional exception which should result in HTTP 417");
			}
			else {
				return orderDao.getOrderByOrderType(value);
			}
		};
	}

	@Bean
	public Supplier<List<Object>> getAllOrders() {
		return () -> {
				return Collections.singletonList(orderDao.getAllOrders());
		};
	}
}
