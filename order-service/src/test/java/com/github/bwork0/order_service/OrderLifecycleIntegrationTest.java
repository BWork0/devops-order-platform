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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("fake")
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, FakeProductServiceConfig.class})
@Transactional
class OrderLifecycleIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	MockRestServiceServer productServer;

	@BeforeEach
	void resetProductServer() {
		productServer.reset();
	}

	@Test
	void orderCanBeWalkedToCompletion() throws Exception {
		ProductStubs.keyboard(productServer);
		int id = createPendingOrder();

		mockMvc.perform(put("/api/orders/{id}/status", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"status\": \"CONFIRMED\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("CONFIRMED"));

		mockMvc.perform(put("/api/orders/{id}/status", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"status\": \"COMPLETED\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("COMPLETED"));
	}

	@Test
	void invalidTransitionIsRejected() throws Exception {
		ProductStubs.keyboard(productServer);
		int id = createPendingOrder();

		mockMvc.perform(put("/api/orders/{id}/status", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"status\": \"CONFIRMED\"}"))
			.andExpect(status().isOk());

		mockMvc.perform(put("/api/orders/{id}/status", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"status\": \"PENDING\"}"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error").value("INVALID_STATUS_TRANSITION"));

		mockMvc.perform(put("/api/orders/{id}/status", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"status\": \"CANCELLED\"}"))
			.andExpect(status().isOk());

		mockMvc.perform(put("/api/orders/{id}/status", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"status\": \"CONFIRMED\"}"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.error").value("INVALID_STATUS_TRANSITION"));
	}

	@Test
	void unknownStatusValueIsRejected() throws Exception {
		ProductStubs.keyboard(productServer);
		int id = createPendingOrder();

		mockMvc.perform(put("/api/orders/{id}/status", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"status\": \"SHIPPED\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("MALFORMED_REQUEST"));
	}

	@Test
	void statusChangeOnMissingOrderReturnsNotFound() throws Exception {
		mockMvc.perform(put("/api/orders/{id}/status", 999999)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"status\": \"CONFIRMED\"}"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("ORDER_NOT_FOUND"));
	}

	@Test
	void ordersCanBeHardDeletedAtEveryStatus() throws Exception {
		for (int i = 0; i < 4; i++) {
			ProductStubs.keyboard(productServer);
		}

		int pending = createPendingOrder();
		deleteAndExpectGone(pending);

		int confirmed = createPendingOrder();
		changeStatus(confirmed, "CONFIRMED");
		deleteAndExpectGone(confirmed);

		int cancelled = createPendingOrder();
		changeStatus(cancelled, "CANCELLED");
		deleteAndExpectGone(cancelled);

		int completed = createPendingOrder();
		changeStatus(completed, "CONFIRMED");
		changeStatus(completed, "COMPLETED");
		deleteAndExpectGone(completed);

		mockMvc.perform(get("/api/orders"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void deletingMissingOrderReturnsNotFound() throws Exception {
		mockMvc.perform(delete("/api/orders/{id}", 999999))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("ORDER_NOT_FOUND"));
	}

	private int createPendingOrder() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"customerName": "John Doe", "customerEmail": "john@example.com", "items": [{"productId": 1, "quantity": 1}]}
						"""))
			.andExpect(status().isCreated())
			.andReturn();
		return (int) JsonPath.read(result.getResponse().getContentAsString(), "$.id");
	}

	private void changeStatus(int id, String status) throws Exception {
		mockMvc.perform(put("/api/orders/{id}/status", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"status\": \"" + status + "\"}"))
			.andExpect(status().isOk());
	}

	private void deleteAndExpectGone(int id) throws Exception {
		mockMvc.perform(delete("/api/orders/{id}", id))
			.andExpect(status().isNoContent());
	}

}