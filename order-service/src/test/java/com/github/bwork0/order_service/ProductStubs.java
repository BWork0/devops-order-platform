package com.github.bwork0.order_service;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.io.IOException;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

final class ProductStubs {

	private static final String PRODUCT_SERVICE = "http://localhost:8080";

	private ProductStubs() {
	}

	static void keyboard(MockRestServiceServer server) {
		keyboard(server, 50);
	}

	static void keyboard(MockRestServiceServer server, int stockQuantity) {
		respond(server, 1, withSuccess(productPayload(1, "Mechanical Keyboard", "89.99", stockQuantity), MediaType.APPLICATION_JSON));
	}

	static void mouse(MockRestServiceServer server) {
		respond(server, 5, withSuccess(productPayload(5, "Wireless Mouse", "39.99", 20), MediaType.APPLICATION_JSON));
	}

	static void notFound(MockRestServiceServer server, long productId) {
		respond(server, productId, withStatus(HttpStatus.NOT_FOUND));
	}

	static void serverError(MockRestServiceServer server, long productId) {
		respond(server, productId, withServerError());
	}

	static void connectionFailure(MockRestServiceServer server, long productId, IOException cause) {
		respond(server, productId, withException(cause));
	}

	private static void respond(MockRestServiceServer server, long productId, org.springframework.test.web.client.ResponseCreator response) {
		server.expect(requestTo(PRODUCT_SERVICE + "/api/products/" + productId)).andRespond(response);
	}

	private static String productPayload(long id, String name, String price, int stockQuantity) {
		return "{\"id\":" + id + ",\"name\":\"" + name + "\",\"price\":" + price + ",\"stockQuantity\":" + stockQuantity + "}";
	}
}