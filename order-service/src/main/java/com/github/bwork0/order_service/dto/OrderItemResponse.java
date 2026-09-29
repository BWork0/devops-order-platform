package com.github.bwork0.order_service.dto;

import com.github.bwork0.order_service.entity.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
		Long id,
		Long productId,
		String productName,
		Integer quantity,
		BigDecimal unitPrice,
		BigDecimal subtotal) {

	public static OrderItemResponse from(OrderItem item) {
		return new OrderItemResponse(
				item.getId(),
				item.getProductId(),
				item.getProductName(),
				item.getQuantity(),
				item.getUnitPrice(),
				item.getSubtotal());
	}
}