package ru.hits.recipe_book.api.product;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.hits.recipe_book.api.ExternalApiTestSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Product get all with filtering and sorting API")
public class ProductGetAllTest extends ExternalApiTestSupport {

    private String scopeToken;
    private String beetName;
    private String porkName;

    @BeforeEach
    void createProductsOnce() throws Exception {
        scopeToken = "api-" + UUID.randomUUID().toString().substring(0, 8);
        beetName = "Свёкла " + scopeToken;
        porkName = "Свинина " + scopeToken;

        HttpResponse<String> firstResponse = post("/api/products", buildProductRequest(
                beetName,
                List.of(),
                43.0,
                0.1,
                1.0,
                1.5,
                "Единица продукта",
                "VEGETABLES",
                "REQUIRES_COOKING",
                List.of("VEGAN")
        ));
        assertEquals(201, firstResponse.statusCode());

        HttpResponse<String> secondResponse = post("/api/products", buildProductRequest(
                porkName,
                List.of(),
                110.0,
                20.0,
                10.0,
                11.5,
                "Единица продукта",
                "MEAT",
                "READY_TO_EAT",
                List.of("SUGAR_FREE")
        ));
        assertEquals(201, secondResponse.statusCode());
    }

    @Test
    @DisplayName("should filter products by category")
    void shouldFilterProductsByCategory() throws Exception {
        HttpResponse<String> response = get("/api/products?category=VEGETABLES&search=" + encodedScopeToken());
        JsonNode body = readBody(response);

        assertEquals(200, response.statusCode());
        assertEquals(1, body.size());
        assertEquals(beetName, body.get(0).get("name").asText());
        assertEquals("VEGETABLES", body.get(0).get("category").asText());
    }

    @Test
    @DisplayName("should filter products by cooking requirement")
    void shouldFilterProductsByCookingRequirement() throws Exception {
        HttpResponse<String> response = get("/api/products?cookingRequirement=READY_TO_EAT&search=" + encodedScopeToken());
        JsonNode body = readBody(response);

        assertEquals(200, response.statusCode());
        assertEquals(1, body.size());
        assertEquals(porkName, body.get(0).get("name").asText());
        assertEquals("READY_TO_EAT", body.get(0).get("cookingRequirement").asText());
    }

    @Test
    @DisplayName("should filter products by diet flag")
    void shouldFilterProductsByDietFlag() throws Exception {
        HttpResponse<String> response = get("/api/products?flags=VEGAN&search=" + encodedScopeToken());
        JsonNode body = readBody(response);

        assertEquals(200, response.statusCode());
        assertEquals(1, body.size());
        assertEquals(beetName, body.get(0).get("name").asText());
        assertContainsInAnyOrder(body.get(0).get("flags"), List.of("VEGAN"));
    }

    @Test
    @DisplayName("should return bad request when get invalid filter field")
    void shouldReturnBadRequestWhenGetInvalidFilterField() throws Exception {
        HttpResponse<String> response = get("/api/products?cookingRequirement=INVALID_FLAG&search=" + encodedScopeToken());
        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("should search products by name ignoring case")
    void shouldSearchProductsByNameIgnoringCase() throws Exception {
        String search = encode(porkName.toLowerCase());

        HttpResponse<String> response = get("/api/products?search=" + search);
        JsonNode body = readBody(response);

        assertEquals(200, response.statusCode());
        assertEquals(1, body.size());
        assertEquals(porkName, body.get(0).get("name").asText());
    }

    @Test
    @DisplayName("should sort products by calories descending")
    void shouldSortProductsByCaloriesDescending() throws Exception {
        HttpResponse<String> response = get("/api/products?sortBy=calories&direction=desc&search=" + encodedScopeToken());
        JsonNode body = readBody(response);

        assertEquals(200, response.statusCode());
        assertEquals(porkName, body.get(0).get("name").asText());
        assertEquals(beetName, body.get(1).get("name").asText());
    }

    @Test
    @DisplayName("should return bad request when sorting by invalid field")
    void shouldReturnBadRequestWhenSortingByInvalidField() throws Exception {
        HttpResponse<String> response = get("/api/products?sortBy=invalid&direction=asc&search=" + encodedScopeToken());
        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("should return empty list when no products match search")
    void shouldReturnEmptyListWhenNoProductsMatchSearch() throws Exception {
        String search = encode("несуществующий-" + scopeToken);

        HttpResponse<String> response = get("/api/products?search=" + search);
        JsonNode body = readBody(response);

        assertEquals(200, response.statusCode());
        assertEquals(0, body.size());
    }

    private String encodedScopeToken() {
        return encode(scopeToken);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
