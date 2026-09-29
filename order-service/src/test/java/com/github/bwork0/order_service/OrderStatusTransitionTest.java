package com.github.bwork0.order_service;

import com.github.bwork0.order_service.entity.Order;
import com.github.bwork0.order_service.entity.OrderStatus;
import com.github.bwork0.order_service.exception.InvalidOrderStatusException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderStatusTransitionTest {

	private Order orderIn(OrderStatus status) {
		Order order = new Order();
		order.setStatus(status);
		return order;
	}

	@Test
	void validTransitionsAreAccepted() {
		assertDoesNotThrow(() -> orderIn(OrderStatus.PENDING).changeStatus(OrderStatus.CONFIRMED));
		assertDoesNotThrow(() -> orderIn(OrderStatus.PENDING).changeStatus(OrderStatus.CANCELLED));
		assertDoesNotThrow(() -> orderIn(OrderStatus.CONFIRMED).changeStatus(OrderStatus.COMPLETED));
		assertDoesNotThrow(() -> orderIn(OrderStatus.CONFIRMED).changeStatus(OrderStatus.CANCELLED));
	}

	@Test
	void pendingCannotSkipToTerminalStates() {
		assertThrows(InvalidOrderStatusException.class, () -> orderIn(OrderStatus.PENDING).changeStatus(OrderStatus.COMPLETED));
	}

	@Test
	void terminalStatesCannotLeave() {
		for (OrderStatus target : OrderStatus.values()) {
			assertThrows(InvalidOrderStatusException.class, () -> orderIn(OrderStatus.COMPLETED).changeStatus(target),
					() -> "COMPLETED -> " + target + " must be invalid");
			assertThrows(InvalidOrderStatusException.class, () -> orderIn(OrderStatus.CANCELLED).changeStatus(target),
					() -> "CANCELLED -> " + target + " must be invalid");
		}
	}

	@Test
	void confirmedCannotReturnToPending() {
		assertThrows(InvalidOrderStatusException.class, () -> orderIn(OrderStatus.CONFIRMED).changeStatus(OrderStatus.PENDING));
	}

	@Test
	void acceptedTransitionChangesStatus() {
		Order order = orderIn(OrderStatus.PENDING);
		order.changeStatus(OrderStatus.CONFIRMED);
		assertEquals(OrderStatus.CONFIRMED, order.getStatus());
	}

}