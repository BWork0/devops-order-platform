package com.github.bwork0.order_service.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
		LocalDateTime timestamp,
		int status,
		String error,
		String message,
		String path) {
}