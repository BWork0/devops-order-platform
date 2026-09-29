package com.github.bwork0.order_service.client;

import java.math.BigDecimal;

public record ProductDto(
		Long id,
		String name,
		BigDecimal price,
		Integer stockQuantity) {
}