package com.github.bwork0.order_service;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("fake")
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, FakeProductServiceConfig.class})
@Transactional
class OrderApiIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	MockRestServiceServer productServer;

	@BeforeEach
	void resetProductServer() {
		productServer.reset();
	}

	@Test
	void createdOrderStartsPendingWithSnapshottedItemsAndServerTotal() throws Exception {
		ProductStubs.keyboard(productServer);
		ProductStubs.mouse(productServer);

		String response = mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "customerName": "John Doe",
						  "customerEmail": "john@example.com",
						  "items": [
						    {"productId": 1, "quantity": 2},
						    {"productId": 5, "quantity": 1}
						  ]
						}
						"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.customerName").value("John Doe"))
			.andExpect(jsonPath("$.customerEmail").value("john@example.com"))
			.andExpect(jsonPath("$.status").value("PENDING"))
			.andExpect(jsonPath("$.totalPrice").value(219.97))
			.andExpect(jsonPath("$.items.length()").value(2))
			.andExpect(jsonPath("$.items[0].productId").value(1))
			.andExpect(jsonPath("$.items[0].productName").value("Mechanical Keyboard"))
			.andExpect(jsonPath("$.items[0].unitPrice").value(89.99))
			.andExpect(jsonPath("$.items[0].quantity").value(2))
			.andExpect(jsonPath("$.items[0].subtotal").value(179.98))
			.andExpect(jsonPath("$.items[1].productId").value(5))
			.andExpect(jsonPath("$.items[1].productName").value("Wireless Mouse"))
			.andExpect(jsonPath("$.items[1].quantity").value(1))
			.andExpect(jsonPath("$.items[1].unitPrice").value(39.99))
			.andExpect(jsonPath("$.items[1].subtotal").value(39.99))
			.andReturn().getResponse().getContentAsString();

		int id = JsonPath.read(response, "$.id");

		mockMvc.perform(get("/api/orders/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(id))
			.andExpect(jsonPath("$.customerName").value("John Doe"))
			.andExpect(jsonPath("$.status").value("PENDING"))
			.andExpect(jsonPath("$.totalPrice").value(219.97))
			.andExpect(jsonPath("$.items.length()").value(2));
	}

	@Test
	void validationFailuresAreRejected() throws Exception {
		mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"customerName": "John Doe", "customerEmail": "john@example.com"}
						"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));

		mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"customerName": "John Doe", "customerEmail": "not-an-email", "items": [{"productId": 1, "quantity": 1}]}
						"""))
			.andExpect(status().isBadRequest());

		mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"customerName": "", "customerEmail": "john@example.com", "items": [{"productId": 1, "quantity": 1}]}
						"""))
			.andExpect(status().isBadRequest());

		ProductStubs.keyboard(productServer);
		mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"customerName": "John Doe", "customerEmail": "john@example.com", "items": [{"productId": 1, "quantity": 0}]}
						"""))
			.andExpect(status().isBadRequest());
	}

	@Test
	void missingReferencedProductRejectsWholeOrderAndPersistsNothing() throws Exception {
		ProductStubs.keyboard(productServer);
		ProductStubs.notFound(productServer, 999);

		mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"customerName": "John Doe", "customerEmail": "john@example.com", "items": [{"productId": 1, "quantity": 1}, {"productId": 999, "quantity": 1}]}
						"""))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"))
			.andExpect(jsonPath("$.message").value("Product with id 999 was not found"));

		mockMvc.perform(get("/api/orders"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void insufficientStockIsRejected() throws Exception {
		ProductStubs.keyboard(productServer, 1);

		mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"customerName": "John Doe", "customerEmail": "john@example.com", "items": [{"productId": 1, "quantity": 2}]}
						"""))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error").value("INSUFFICIENT_STOCK"));
	}

	@Test
	void unavailableProductServiceMapsTo503() throws Exception {
		ProductStubs.serverError(productServer, 1);

		mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"customerName": "John Doe", "customerEmail": "john@example.com", "items": [{"productId": 1, "quantity": 1}]}
						"""))
			.andExpect(status().isServiceUnavailable())
			.andExpect(jsonPath("$.error").value("PRODUCT_SERVICE_UNAVAILABLE"));
	}

	@Test
	void duplicateProductReferencesRemainSeparateItems() throws Exception {
		ProductStubs.keyboard(productServer);
		ProductStubs.keyboard(productServer);

		mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"customerName": "John Doe", "customerEmail": "john@example.com", "items": [{"productId": 1, "quantity": 1}, {"productId": 1, "quantity": 1}]}
						"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.items.length()").value(2))
			.andExpect(jsonPath("$.totalPrice").value(179.98));
	}

	@Test
	void ordersCanBeListed() throws Exception {
		ProductStubs.keyboard(productServer);

		mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"customerName": "John Doe", "customerEmail": "john@example.com", "items": [{"productId": 1, "quantity": 1}]}
						"""))
			.andExpect(status().isCreated());

		mockMvc.perform(get("/api/orders"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].customerName").value("John Doe"))
			.andExpect(jsonPath("$[0].totalPrice").value(89.99))
			.andExpect(jsonPath("$[0].items.length()").value(1));
	}

	@Test
	void missingOrderReturnsNotFound() throws Exception {
		mockMvc.perform(get("/api/orders/{id}", 999999))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("ORDER_NOT_FOUND"))
			.andExpect(jsonPath("$.message").value("Order with id 999999 was not found"))
			.andExpect(jsonPath("$.path").value("/api/orders/999999"));
	}

	@Test
	void productTimeoutMapsTo503() throws Exception {
		ProductStubs.connectionFailure(productServer, 1, new SocketTimeoutException("read timed out"));

		mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"customerName": "John Doe", "customerEmail": "john@example.com", "items": [{"productId": 1, "quantity": 1}]}
						"""))
			.andExpect(status().isServiceUnavailable())
			.andExpect(jsonPath("$.error").value("PRODUCT_SERVICE_UNAVAILABLE"));
	}

	@Test
	void refusedProductConnectionMapsTo503() throws Exception {
		ProductStubs.connectionFailure(productServer, 1, new ConnectException("Connection refused"));

		mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"customerName": "John Doe", "customerEmail": "john@example.com", "items": [{"productId": 1, "quantity": 1}]}
						"""))
			.andExpect(status().isServiceUnavailable())
			.andExpect(jsonPath("$.error").value("PRODUCT_SERVICE_UNAVAILABLE"));
	}

	@Test
	void malformedOrderIdIsRejected() throws Exception {
		mockMvc.perform(get("/api/orders/{id}", "abc"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("MALFORMED_REQUEST"));
	}

}