package com.github.bwork0.order_service;

import com.github.bwork0.order_service.client.ProductServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/**
 * Replaces the production ProductServiceClient with one backed by a
 * MockRestServiceServer. The RestClient.Builder bean below must stay a single
 * shared instance: bindTo mutates it, and the client bean depends on the
 * bound server (boundServer parameter) so it is created after binding.
 */
@TestConfiguration(proxyBeanMethods = false)
@Profile("fake")
class FakeProductServiceConfig {

	@Bean
	RestClient.Builder restClientBuilder() {
		return RestClient.builder();
	}

	@Bean
	MockRestServiceServer mockRestServiceServer(RestClient.Builder builder) {
		return MockRestServiceServer.bindTo(builder).build();
	}

	@Bean
	ProductServiceClient productServiceClient(RestClient.Builder builder,
			@Value("${product-service.base-url}") String baseUrl, MockRestServiceServer boundServer) {
		return new ProductServiceClient(builder.baseUrl(baseUrl).build());
	}
}