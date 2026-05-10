package ru.hits.recipe_book.api.dish;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.hits.recipe_book.api.ExternalApiTestSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Dish update API")
public class DishUpdateTest extends ExternalApiTestSupport {

    @Test
    @DisplayName("should update dish with valid data")
    void shouldUpdateDishWithValidData() throws Exception {
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

        HttpResponse<String> response = put("/api/dishes/" + dishId, buildDishRequest(
                "Салат",
                List.of("https://example.com/dish-2.jpg"),
                null,
                null,
                null,
                null,
                List.of(ingredient(productId, 150.0)),
                150.0,
                "SALAD",
                List.of("VEGAN")
        ));

        JsonNode body = readBody(response);
        assertEquals(200, response.statusCode());
        assertEquals(dishId, body.get("id").asText());
        assertEquals("Салат", body.get("name").asText());
        assertEquals("SALAD", body.get("category").asText());
        assertEquals(150.0, body.get("servingSizeGrams").asDouble());
    }

    @Test
    @DisplayName("should return bad request when updating dish with empty ingredients")
    void shouldReturnBadRequestWhenUpdatingDishWithEmptyIngredients() throws Exception {
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

        HttpResponse<String> response = put("/api/dishes/" + dishId, buildDishRequest(
                "Борщ",
                List.of(),
                null,
                null,
                null,
                null,
                List.of(),
                100.0,
                "SOUP",
                List.of("VEGAN")
        ));

        JsonNode body = readBody(response);
        assertEquals(400, response.statusCode());
        assertEquals("Validation error", body.get("title").asText());
    }

    @Test
    @DisplayName("should return not found when updating unknown dish")
    void shouldReturnNotFoundWhenUpdatingUnknownDish() throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = put("/api/dishes/" + UUID.randomUUID(), buildDishRequest(
                "Борщ",
                List.of(),
                null,
                null,
                null,
                null,
                List.of(ingredient(productId, 100.0)),
                100.0,
                "SOUP",
                List.of("VEGAN")
        ));

        assertEquals(404, response.statusCode());
    }
}
