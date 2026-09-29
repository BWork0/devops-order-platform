package com.github.bwork0.order_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining("; "));
		return error(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message, request);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleMalformed(HttpMessageNotReadableException ex, HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Malformed request body", request);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Malformed request", request);
	}

	@ExceptionHandler(ProductNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleProductNotFound(ProductNotFoundException ex, HttpServletRequest request) {
		return error(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", ex.getMessage(), request);
	}

	@ExceptionHandler(OrderNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleOrderNotFound(OrderNotFoundException ex, HttpServletRequest request) {
		return error(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", ex.getMessage(), request);
	}

	@ExceptionHandler(InvalidOrderStatusException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorResponse handleInvalidTransition(InvalidOrderStatusException ex, HttpServletRequest request) {
		return error(HttpStatus.CONFLICT, "INVALID_STATUS_TRANSITION", ex.getMessage(), request);
	}

	@ExceptionHandler(InsufficientStockException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorResponse handleInsufficientStock(InsufficientStockException ex, HttpServletRequest request) {
		return error(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", ex.getMessage(), request);
	}

	@ExceptionHandler(ProductServiceUnavailableException.class)
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public ErrorResponse handleProductUnavailable(ProductServiceUnavailableException ex, HttpServletRequest request) {
		return error(HttpStatus.SERVICE_UNAVAILABLE, "PRODUCT_SERVICE_UNAVAILABLE", ex.getMessage(), request);
	}

	private static ErrorResponse error(HttpStatus status, String code, String message, HttpServletRequest request) {
		return new ErrorResponse(LocalDateTime.now(), status.value(), code, message, request.getRequestURI());
	}
}