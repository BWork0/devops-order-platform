package com.github.bwork0.order_service.dto;

import com.github.bwork0.order_service.entity.Order;
import com.github.bwork0.order_service.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
		Long id,
		String customerName,
		String customerEmail,
		OrderStatus status,
		BigDecimal totalPrice,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		List<OrderItemResponse> items) {

	public static OrderResponse from(Order order) {
		return new OrderResponse(
				order.getId(),
				order.getCustomerName(),
				order.getCustomerEmail(),
				order.getStatus(),
				order.getTotalPrice(),
				order.getCreatedAt(),
				order.getUpdatedAt(),
				order.getItems().stream()
						.map(OrderItemResponse::from)
						.toList());
	}
}