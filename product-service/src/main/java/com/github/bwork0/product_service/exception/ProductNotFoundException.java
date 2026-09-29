package com.github.bwork0.product_service.exception;

public class ProductNotFoundException extends RuntimeException {

	public ProductNotFoundException(Long id) {
		super("Product with id " + id + " was not found");
	}
}