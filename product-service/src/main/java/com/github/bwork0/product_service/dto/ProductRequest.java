package com.github.bwork0.product_service.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProductRequest(
		@NotBlank String name,
		String description,
		@NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal price,
		@NotNull @Min(0) Integer stockQuantity) {
}