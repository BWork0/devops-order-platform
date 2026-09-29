package com.github.bwork0.order_service.service;

import com.github.bwork0.order_service.client.ProductDto;
import com.github.bwork0.order_service.client.ProductServiceClient;
import com.github.bwork0.order_service.dto.CreateOrderRequest;
import com.github.bwork0.order_service.dto.OrderItemRequest;
import com.github.bwork0.order_service.dto.OrderResponse;
import com.github.bwork0.order_service.entity.Order;
import com.github.bwork0.order_service.entity.OrderItem;
import com.github.bwork0.order_service.entity.OrderStatus;
import com.github.bwork0.order_service.exception.InsufficientStockException;
import com.github.bwork0.order_service.exception.OrderNotFoundException;
import com.github.bwork0.order_service.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

	private final OrderRepository orderRepository;
	private final ProductServiceClient productClient;

	public OrderService(OrderRepository orderRepository, ProductServiceClient productClient) {
		this.orderRepository = orderRepository;
		this.productClient = productClient;
	}

	@Transactional
	public OrderResponse create(CreateOrderRequest request) {
		Order order = new Order();
		order.setCustomerName(request.customerName());
		order.setCustomerEmail(request.customerEmail());
		order.setStatus(OrderStatus.PENDING);

		BigDecimal total = BigDecimal.ZERO;
		for (OrderItemRequest itemRequest : request.items()) {
			ProductDto product = productClient.getProduct(itemRequest.productId());
			if (product.stockQuantity() < itemRequest.quantity()) {
				throw new InsufficientStockException(product.id(), product.stockQuantity(), itemRequest.quantity());
			}
			OrderItem item = new OrderItem();
			item.setProductId(product.id());
			item.setProductName(product.name());
			item.setQuantity(itemRequest.quantity());
			item.setUnitPrice(product.price());
			item.setSubtotal(product.price().multiply(BigDecimal.valueOf(itemRequest.quantity())));
			order.addItem(item);
			total = total.add(item.getSubtotal());
		}
		order.setTotalPrice(total);

		return OrderResponse.from(orderRepository.save(order));
	}

	@Transactional(readOnly = true)
	public List<OrderResponse> findAll() {
		return orderRepository.findAll().stream()
				.map(OrderResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public OrderResponse findById(Long id) {
		return orderRepository.findById(id)
				.map(OrderResponse::from)
				.orElseThrow(() -> new OrderNotFoundException(id));
	}

	@Transactional
	public OrderResponse updateStatus(Long id, OrderStatus target) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new OrderNotFoundException(id));
		order.changeStatus(target);
		return OrderResponse.from(orderRepository.saveAndFlush(order));
	}

	@Transactional
	public void delete(Long id) {
		if (!orderRepository.existsById(id)) {
			throw new OrderNotFoundException(id);
		}
		orderRepository.deleteById(id);
	}
}