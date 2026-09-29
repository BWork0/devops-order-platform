package com.github.bwork0.product_service.service;

import com.github.bwork0.product_service.dto.ProductRequest;
import com.github.bwork0.product_service.dto.ProductResponse;
import com.github.bwork0.product_service.entity.Product;
import com.github.bwork0.product_service.exception.ProductNotFoundException;
import com.github.bwork0.product_service.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

	private final ProductRepository repository;

	public ProductService(ProductRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public ProductResponse create(ProductRequest request) {
		Product product = new Product();
		product.setName(request.name());
		product.setDescription(request.description());
		product.setPrice(request.price());
		product.setStockQuantity(request.stockQuantity());
		return ProductResponse.from(repository.save(product));
	}

	@Transactional(readOnly = true)
	public List<ProductResponse> findAll() {
		return repository.findAll().stream()
				.map(ProductResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public ProductResponse findById(Long id) {
		return repository.findById(id)
				.map(ProductResponse::from)
				.orElseThrow(() -> new ProductNotFoundException(id));
	}

	@Transactional
	public ProductResponse update(Long id, ProductRequest request) {
		Product product = repository.findById(id)
				.orElseThrow(() -> new ProductNotFoundException(id));
		product.setName(request.name());
		product.setDescription(request.description());
		product.setPrice(request.price());
		product.setStockQuantity(request.stockQuantity());
		return ProductResponse.from(repository.saveAndFlush(product));
	}

	@Transactional
	public void delete(Long id) {
		if (!repository.existsById(id)) {
			throw new ProductNotFoundException(id);
		}
		repository.deleteById(id);
	}
}