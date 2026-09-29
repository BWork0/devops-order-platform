package com.github.bwork0.order_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration(proxyBeanMethods = false)
@Profile("!fake")
class ProductClientConfig {

	@Bean
	ProductServiceClient productServiceClient(@Value("${product-service.base-url}") String baseUrl) {
		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
				HttpClient.newBuilder()
						.connectTimeout(Duration.ofSeconds(1))
						.build());
		factory.setReadTimeout(Duration.ofSeconds(2));

		return new ProductServiceClient(
				RestClient.builder()
						.baseUrl(baseUrl)
						.requestFactory(factory)
						.build());
	}
}