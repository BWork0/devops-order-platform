package com.github.bwork0.product_service;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class ProductApiIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@Test
	void createdProductCanBeRetrieved() throws Exception {
		String response = createProduct("""
				{
				  "name": "Mechanical Keyboard",
				  "description": "A mechanical keyboard with brown switches",
				  "price": 89.99,
				  "stockQuantity": 42
				}
				""");

		int id = JsonPath.read(response, "$.id");

		mockMvc.perform(get("/api/products/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(id))
			.andExpect(jsonPath("$.name").value("Mechanical Keyboard"))
			.andExpect(jsonPath("$.description").value("A mechanical keyboard with brown switches"))
			.andExpect(jsonPath("$.price").value(89.99))
			.andExpect(jsonPath("$.stockQuantity").value(42))
			.andExpect(jsonPath("$.createdAt").isNotEmpty())
			.andExpect(jsonPath("$.updatedAt").isNotEmpty());
	}

	@Test
	void invalidProductsAreRejected() throws Exception {
		missingNameIsRejected();
		zeroPriceIsRejected();
		negativePriceIsRejected();
		negativeStockIsRejected();
		overPrecisePriceIsRejected();
	}

	private void missingNameIsRejected() throws Exception {
		mockMvc.perform(post("/api/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"description": "no name"}
						"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
	}

	private void zeroPriceIsRejected() throws Exception {
		mockMvc.perform(post("/api/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name": "Cheap", "price": 0, "stockQuantity": 1}
						"""))
			.andExpect(status().isBadRequest());
	}

	private void negativePriceIsRejected() throws Exception {
		mockMvc.perform(post("/api/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name": "Cheap", "price": -5, "stockQuantity": 1}
						"""))
			.andExpect(status().isBadRequest());
	}

	private void negativeStockIsRejected() throws Exception {
		mockMvc.perform(post("/api/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name": "Negative Stock", "price": 5, "stockQuantity": -1}
						"""))
			.andExpect(status().isBadRequest());
	}

	private void overPrecisePriceIsRejected() throws Exception {
		mockMvc.perform(post("/api/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name": "Precise", "price": 19.999, "stockQuantity": 1}
						"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
	}

	@Test
	void validationErrorHasConsistentShape() throws Exception {
		mockMvc.perform(post("/api/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"price": 1, "stockQuantity": 1}
						"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
			.andExpect(jsonPath("$.message").value("name: must not be blank"))
			.andExpect(jsonPath("$.path").value("/api/products"));
	}

	@Test
	void allProductsAreListed() throws Exception {
		createProduct("{\"name\": \"Keyboard\", \"price\": 89.99, \"stockQuantity\": 5}");
		createProduct("{\"name\": \"Mouse\", \"price\": 39.99, \"stockQuantity\": 3}");

		mockMvc.perform(get("/api/products"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].name").value("Keyboard"))
			.andExpect(jsonPath("$[1].name").value("Mouse"));
	}

	@Test
	void missingProductReturnsNotFound() throws Exception {
		mockMvc.perform(get("/api/products/{id}", 999999))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.timestamp").isNotEmpty())
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"))
			.andExpect(jsonPath("$.message").value("Product with id 999999 was not found"))
			.andExpect(jsonPath("$.path").value("/api/products/999999"));
	}

	@Test
	void productCanBeUpdatedWithFullReplacement() throws Exception {
		String response = createProduct("{\"name\": \"Keyboard\", \"description\": \"old\", \"price\": 89.99, \"stockQuantity\": 5}");
		int id = JsonPath.read(response, "$.id");

		mockMvc.perform(put("/api/products/{id}", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\": \"Keyboard Pro\", \"description\": \"new\", \"price\": 129.99, \"stockQuantity\": 7}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(id))
			.andExpect(jsonPath("$.name").value("Keyboard Pro"))
			.andExpect(jsonPath("$.description").value("new"))
			.andExpect(jsonPath("$.price").value(129.99))
			.andExpect(jsonPath("$.stockQuantity").value(7));
	}

	@Test
	void updatingMissingProductReturnsNotFound() throws Exception {
		mockMvc.perform(put("/api/products/{id}", 999999)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\": \"Ghost\", \"price\": 1, \"stockQuantity\": 1}"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"))
			.andExpect(jsonPath("$.path").value("/api/products/999999"));
	}

	@Test
	void productCanBeDeletedAndThenIsMissing() throws Exception {
		String response = createProduct("{\"name\": \"Disposable\", \"price\": 1.50, \"stockQuantity\": 1}");
		int id = JsonPath.read(response, "$.id");

		mockMvc.perform(delete("/api/products/{id}", id))
			.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/products/{id}", id))
			.andExpect(status().isNotFound());
	}

	@Test
	void deletingMissingProductReturnsNotFound() throws Exception {
		mockMvc.perform(delete("/api/products/{id}", 999999))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"));
	}

	@Test
	void malformedJsonIsRejected() throws Exception {
		mockMvc.perform(post("/api/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\": \"Broken\","))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("MALFORMED_REQUEST"))
			.andExpect(jsonPath("$.path").value("/api/products"));
	}

	private String createProduct(String body) throws Exception {
		return mockMvc.perform(post("/api/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getContentAsString();
	}

	@Test
	void malformedPathIdIsRejected() throws Exception {
		mockMvc.perform(get("/api/products/{id}", "abc"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error").value("MALFORMED_REQUEST"))
			.andExpect(jsonPath("$.path").value("/api/products/abc"));
	}
}