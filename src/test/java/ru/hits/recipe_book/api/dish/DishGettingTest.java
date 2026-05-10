package ru.hits.recipe_book.api.dish;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.hits.recipe_book.api.ExternalApiTestSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Dish get by id API")
public class DishGettingTest extends ExternalApiTestSupport {

    @Test
    @DisplayName("should return dish by valid id")
    void shouldReturnDishById() throws Exception {
        String productId = createProductAndReturnId();
        String dishId = createDishAndReturnId(
                "Борщ",
                null,
                null,
                null,
                null,
                List.of(ingredient(productId, 100.0)),
                100.0,
                "SOUP",
                List.of("VEGAN")
        );

        HttpResponse<String> response = get("/api/dishes/" + dishId);
        JsonNode body = readBody(response);

        assertEquals(200, response.statusCode());
        assertEquals(dishId, body.get("id").asText());
        assertEquals("Борщ", body.get("name").asText());
        assertEquals("SOUP", body.get("category").asText());
        assertEquals(1, body.get("ingredients").size());
    }

    @Test
    @DisplayName("should return not found when getting unknown dish")
    void shouldReturnNotFoundWhenGettingUnknownDish() throws Exception {
        HttpResponse<String> response = get("/api/dishes/" + UUID.randomUUID());
        assertEquals(404, response.statusCode());
    }
}
