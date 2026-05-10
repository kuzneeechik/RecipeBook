package ru.hits.recipe_book.api.product;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.hits.recipe_book.api.ExternalApiTestSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Product delete API")
public class ProductDeleteTest extends ExternalApiTestSupport {

    @Test
    @DisplayName("should delete product not used in dish by valid id")
    void shouldDeleteProductWhenItIsNotUsedInDish() throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> deleteResponse = delete("/api/products/" + productId);
        HttpResponse<String> getResponse = get("/api/products/" + productId);

        assertEquals(204, deleteResponse.statusCode());
        assertEquals(404, getResponse.statusCode());
    }

    @Test
    @DisplayName("should return conflict when deleting product used in dish")
    void shouldReturnConflictWhenDeletingProductUsedInDish() throws Exception {
        String productId = createProductAndReturnId();
        createDishWithProduct(productId, "Борщ");

        HttpResponse<String> response = delete("/api/products/" + productId);
        JsonNode body = readBody(response);

        assertEquals(409, response.statusCode());
        assertEquals("Product is used in dishes", body.get("title").asText());
        assertEquals(1, body.get("dishes").size());
        assertEquals("Борщ", body.get("dishes").get(0).asText());
    }

    @Test
    @DisplayName("should return not found when deleting product by unknown id")
    void shouldReturnNotFoundWhenDeletingProductByUnknownId() throws Exception {
        HttpResponse<String> response = delete("/api/products/" + UUID.randomUUID());
        assertEquals(404, response.statusCode());
    }
}
