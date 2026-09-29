package com.github.bwork0.order_service.controller;

import com.github.bwork0.order_service.dto.CreateOrderRequest;
import com.github.bwork0.order_service.dto.OrderResponse;
import com.github.bwork0.order_service.dto.StatusRequest;
import com.github.bwork0.order_service.entity.OrderStatus;
import com.github.bwork0.order_service.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService service;

	public OrderController(OrderService service) {
		this.service = service;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
		return service.create(request);
	}

	@GetMapping
	public List<OrderResponse> findAll() {
		return service.findAll();
	}

	@GetMapping("/{id}")
	public OrderResponse findById(@PathVariable Long id) {
		return service.findById(id);
	}

	@PutMapping("/{id}/status")
	public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
		return service.updateStatus(id, request.status());
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		service.delete(id);
	}
}