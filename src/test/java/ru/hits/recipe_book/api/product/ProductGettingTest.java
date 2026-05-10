package ru.hits.recipe_book.api.product;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.hits.recipe_book.api.ExternalApiTestSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Product get by id API")
public class ProductGettingTest extends ExternalApiTestSupport {

    @Test
    @DisplayName("should return product by valid id")
    void shouldReturnProductById() throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = get("/api/products/" + productId);
        JsonNode body = readBody(response);

        assertEquals(200, response.statusCode());
        assertEquals(productId, body.get("id").asText());
        assertEquals("Свёкла", body.get("name").asText());
        assertEquals(1, body.get("photos").size());
        assertEquals("https://example.com/photo-1.jpg", body.get("photos").get(0).asText());
        assertEquals(43.0, body.get("calories").asDouble());
        assertEquals(0.1, body.get("proteins").asDouble());
        assertEquals(1.0, body.get("fats").asDouble());
        assertEquals(1.5, body.get("carbohydrates").asDouble());
        assertEquals("Единица продукта", body.get("composition").asText());
        assertEquals("VEGETABLES", body.get("category").asText());
        assertEquals("REQUIRES_COOKING", body.get("cookingRequirement").asText());
        assertContainsInAnyOrder(body.get("flags"), List.of("VEGAN", "GLUTEN_FREE", "SUGAR_FREE"));
    }

    @Test
    @DisplayName("should return not found when getting unknown product")
    void shouldReturnNotFoundWhenGettingProductByUnknownId() throws Exception {
        HttpResponse<String> response = get("/api/products/" + UUID.randomUUID());
        assertEquals(404, response.statusCode());
    }
}
