package com.github.bwork0.order_service.client;

import com.github.bwork0.order_service.exception.ProductNotFoundException;
import com.github.bwork0.order_service.exception.ProductServiceUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

public class ProductServiceClient {

	private final RestClient restClient;

	public ProductServiceClient(RestClient restClient) {
		this.restClient = restClient;
	}

	public ProductDto getProduct(Long id) {
		try {
			return restClient.get()
					.uri("/api/products/{id}", id)
					.retrieve()
					.body(ProductDto.class);
		} catch (HttpStatusCodeException ex) {
			if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
				throw new ProductNotFoundException(id);
			}
			throw new ProductServiceUnavailableException();
		} catch (ResourceAccessException ex) {
			throw new ProductServiceUnavailableException();
		}
	}
}