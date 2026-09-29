package com.github.bwork0.order_service.exception;

public class InsufficientStockException extends RuntimeException {

	public InsufficientStockException(Long productId, Integer available, Integer requested) {
		super("Insufficient stock for product " + productId + ": requested " + requested + ", available " + available);
	}
}