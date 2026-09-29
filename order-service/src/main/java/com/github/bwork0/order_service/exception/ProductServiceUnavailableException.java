package com.github.bwork0.order_service.exception;

public class ProductServiceUnavailableException extends RuntimeException {

	public ProductServiceUnavailableException() {
		super("Product Service is unavailable");
	}
}