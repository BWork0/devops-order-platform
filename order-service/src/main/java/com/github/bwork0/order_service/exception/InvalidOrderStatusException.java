package com.github.bwork0.order_service.exception;

import com.github.bwork0.order_service.entity.OrderStatus;

public class InvalidOrderStatusException extends RuntimeException {

	public InvalidOrderStatusException(OrderStatus from, OrderStatus to) {
		super("Cannot transition order from " + from + " to " + to);
	}
}