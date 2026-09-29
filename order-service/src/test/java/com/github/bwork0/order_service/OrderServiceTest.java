package com.github.bwork0.order_service;

import com.github.bwork0.order_service.client.ProductDto;
import com.github.bwork0.order_service.client.ProductServiceClient;
import com.github.bwork0.order_service.dto.CreateOrderRequest;
import com.github.bwork0.order_service.dto.OrderItemRequest;
import com.github.bwork0.order_service.dto.OrderResponse;
import com.github.bwork0.order_service.exception.InsufficientStockException;
import com.github.bwork0.order_service.repository.OrderRepository;
import com.github.bwork0.order_service.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	ProductServiceClient productClient;

	@Mock
	OrderRepository orderRepository;

	@InjectMocks
	OrderService service;

	@Test
	void orderTotalIsSumOfWorkedExampleSubtotals() {
		when(productClient.getProduct(1L)).thenReturn(new ProductDto(1L, "Mechanical Keyboard", new BigDecimal("89.99"), 50));
		when(productClient.getProduct(5L)).thenReturn(new ProductDto(5L, "Wireless Mouse", new BigDecimal("39.99"), 20));
		when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		OrderResponse response = service.create(new CreateOrderRequest("John Doe", "john@example.com",
				List.of(new OrderItemRequest(1L, 2), new OrderItemRequest(5L, 1))));

		assertEquals(new BigDecimal("219.97"), response.totalPrice());
		assertEquals(new BigDecimal("179.98"), response.items().get(0).subtotal());
		assertEquals(new BigDecimal("39.99"), response.items().get(1).subtotal());
		assertEquals("PENDING", response.status().name());
	}

	@Test
	void duplicateProductReferencesRemainSeparateItems() {
		when(productClient.getProduct(1L)).thenReturn(new ProductDto(1L, "Mechanical Keyboard", new BigDecimal("89.99"), 50));
		when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		OrderResponse response = service.create(new CreateOrderRequest("John Doe", "john@example.com",
				List.of(new OrderItemRequest(1L, 1), new OrderItemRequest(1L, 1))));

		assertEquals(2, response.items().size());
		assertEquals(new BigDecimal("179.98"), response.totalPrice());
	}

	@Test
	void stockIsCheckedAgainstRequestedQuantity() {
		when(productClient.getProduct(1L)).thenReturn(new ProductDto(1L, "Mechanical Keyboard", new BigDecimal("89.99"), 1));

		assertThrows(InsufficientStockException.class, () -> service.create(new CreateOrderRequest("John Doe", "john@example.com",
				List.of(new OrderItemRequest(1L, 2)))));

		verifyNoInteractions(orderRepository);
	}

}