package com.github.bwork0.product_service.dto;

import com.github.bwork0.product_service.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
		Long id,
		String name,
		String description,
		BigDecimal price,
		Integer stockQuantity,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {

	public static ProductResponse from(Product product) {
		return new ProductResponse(
				product.getId(),
				product.getName(),
				product.getDescription(),
				product.getPrice(),
				product.getStockQuantity(),
				product.getCreatedAt(),
				product.getUpdatedAt());
	}
}