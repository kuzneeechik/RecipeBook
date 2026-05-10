package ru.hits.recipe_book.api.dish;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.hits.recipe_book.api.ExternalApiTestSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Dish create API")
public class DishCreationTest extends ExternalApiTestSupport {

    @Test
    @DisplayName("should create dish with auto calculated nutrition and filtered flags")
    void shouldCreateDishWithAutoCalculatedNutritionAndFilteredFlags() throws Exception {
        String productId = createProductAndReturnId(
                "Тофу",
                100.0,
                10.0,
                5.0,
                20.0,
                "VEGETABLES",
                "READY_TO_EAT",
                List.of("VEGAN", "GLUTEN_FREE")
        );

        HttpResponse<String> response = post("/api/dishes", buildDishRequest(
                "Тёплый тофу",
                List.of("https://example.com/dish-1.jpg"),
                null,
                null,
                null,
                null,
                List.of(ingredient(productId, 150.0)),
                150.0,
                "SALAD",
                List.of("VEGAN", "GLUTEN_FREE", "SUGAR_FREE")
        ));

        JsonNode body = readBody(response);

        assertEquals(201, response.statusCode());
        assertEquals("Тёплый тофу", body.get("name").asText());
        assertEquals(150.0, body.get("calories").asDouble());
        assertEquals(15.0, body.get("proteins").asDouble());
        assertEquals(7.5, body.get("fats").asDouble());
        assertEquals(30.0, body.get("carbohydrates").asDouble());
        assertEquals(150.0, body.get("servingSizeGrams").asDouble());
        assertEquals("SALAD", body.get("category").asText());
        assertContainsInAnyOrder(body.get("flags"), List.of("VEGAN", "GLUTEN_FREE"));
        assertContainsInAnyOrder(body.get("availableFlags"), List.of("VEGAN", "GLUTEN_FREE"));
        assertEquals(1, body.get("ingredients").size());
        assertEquals(productId, body.get("ingredients").get(0).get("productId").asText());
        assertEquals("Тофу", body.get("ingredients").get(0).get("productName").asText());
        assertEquals(150.0, body.get("ingredients").get(0).get("quantityGrams").asDouble());
    }

    @ParameterizedTest
    @CsvSource({
            "Су",
            "Суп"
    })
    @DisplayName("should create dish with valid name length")
    void shouldCreateDishWithValidNameLength(String name) throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = post("/api/dishes", buildDishRequest(
                name,
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

        JsonNode body = readBody(response);
        assertEquals(201, response.statusCode());
        assertEquals(name, body.get("name").asText());
    }

    @Test
    @DisplayName("should return bad request when dish name length is less than two")
    void shouldReturnBadRequestWhenDishNameLengthIsLessThanTwo() throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = post("/api/dishes", buildDishRequest(
                "Я",
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

        JsonNode body = readBody(response);
        assertEquals(400, response.statusCode());
        assertEquals("Validation error", body.get("title").asText());
        assertEquals("Request body validation failed", body.get("detail").asText());
        assertTrue(body.get("errors").has("name"));
    }

    @ParameterizedTest
    @CsvSource({
            "0",
            "1",
            "4",
            "5"
    })
    @DisplayName("should create dish with valid number of photos")
    void shouldCreateDishWithValidNumberOfPhotos(int photoCount) throws Exception {
        String productId = createProductAndReturnId();
        List<String> photos = buildPhotosList(photoCount);

        HttpResponse<String> response = post("/api/dishes", buildDishRequest(
                "Овощной суп",
                photos,
                null,
                null,
                null,
                null,
                List.of(ingredient(productId, 100.0)),
                100.0,
                "SOUP",
                List.of("VEGAN")
        ));

        JsonNode body = readBody(response);
        assertEquals(201, response.statusCode());
        assertEquals(photoCount, body.get("photos").size());
        assertContainsExactly(body.get("photos"), photos);
    }

    @Test
    @DisplayName("should return bad request when dish has more than five photos")
    void shouldReturnBadRequestWhenDishHasMoreThanFivePhotos() throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = post("/api/dishes", buildDishRequest(
                "Овощной суп",
                buildPhotosList(6),
                null,
                null,
                null,
                null,
                List.of(ingredient(productId, 100.0)),
                100.0,
                "SOUP",
                List.of("VEGAN")
        ));

        JsonNode body = readBody(response);
        assertEquals(400, response.statusCode());
        assertEquals("Validation error", body.get("title").asText());
        assertEquals("Request body validation failed", body.get("detail").asText());
        assertTrue(body.get("errors").has("photos"));
    }

    @ParameterizedTest
    @CsvSource({
            "0.01",
            "100.0",
            "100.01"
    })
    @DisplayName("should create dish with valid ingredient quantity boundary values")
    void shouldCreateDishWithValidIngredientQuantityBoundaryValues(double quantityGrams) throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = post("/api/dishes", buildDishRequest(
                "Грибной суп",
                List.of(),
                null,
                null,
                null,
                null,
                List.of(ingredient(productId, quantityGrams)),
                150.0,
                "SOUP",
                List.of("VEGAN")
        ));

        JsonNode body = readBody(response);
        assertEquals(201, response.statusCode());
        assertEquals(quantityGrams, body.get("ingredients").get(0).get("quantityGrams").asDouble());
    }

    @Test
    @DisplayName("should return bad request when ingredient quantity is zero")
    void shouldReturnBadRequestWhenIngredientQuantityIsZero() throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = post("/api/dishes", buildDishRequest(
                "Грибной суп",
                List.of(),
                null,
                null,
                null,
                null,
                List.of(ingredient(productId, 0.0)),
                150.0,
                "SOUP",
                List.of("VEGAN")
        ));

        JsonNode body = readBody(response);
        assertEquals(400, response.statusCode());
        assertEquals("Validation error", body.get("title").asText());
        assertEquals("Request body validation failed", body.get("detail").asText());
    }

    @ParameterizedTest
    @CsvSource({
            "0.01",
            "250.0"
    })
    @DisplayName("should create dish with valid serving size boundary values")
    void shouldCreateDishWithValidServingSizeBoundaryValues(double servingSizeGrams) throws Exception {
        String productId = createProductAndReturnId(
                "Вода",
                0.0,
                0.0,
                0.0,
                0.0,
                "LIQUID",
                "READY_TO_EAT",
                List.of("VEGAN", "GLUTEN_FREE", "SUGAR_FREE")
        );

        HttpResponse<String> response = post("/api/dishes", buildDishRequest(
                "Напиток",
                List.of(),
                null,
                null,
                null,
                null,
                List.of(ingredient(productId, 50.0)),
                servingSizeGrams,
                "DRINK",
                List.of("VEGAN")
        ));

        JsonNode body = readBody(response);
        assertEquals(201, response.statusCode());
        assertEquals(servingSizeGrams, body.get("servingSizeGrams").asDouble());
    }

    @Test
    @DisplayName("should return bad request when serving size is zero")
    void shouldReturnBadRequestWhenServingSizeIsZero() throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = post("/api/dishes", buildDishRequest(
                "Овощной суп",
                List.of(),
                null,
                null,
                null,
                null,
                List.of(ingredient(productId, 100.0)),
                0.0,
                "SOUP",
                List.of("VEGAN")
        ));

        JsonNode body = readBody(response);
        assertEquals(400, response.statusCode());
        assertEquals("Validation error", body.get("title").asText());
        assertEquals("Request body validation failed", body.get("detail").asText());
        assertTrue(body.get("errors").has("servingSizeGrams"));
    }

    @Test
    @DisplayName("should return bad request when ingredients list is empty")
    void shouldReturnBadRequestWhenIngredientsListIsEmpty() throws Exception {
        HttpResponse<String> response = post("/api/dishes", buildDishRequest(
                "Овощной суп",
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
        assertEquals("Request body validation failed", body.get("detail").asText());
        assertTrue(body.get("errors").has("ingredients"));
    }

    @Test
    @DisplayName("should return not found when dish contains unknown product")
    void shouldReturnNotFoundWhenDishContainsUnknownProduct() throws Exception {
        HttpResponse<String> response = post("/api/dishes", buildDishRequest(
                "Овощной суп",
                List.of(),
                null,
                null,
                null,
                null,
                List.of(ingredient(UUID.randomUUID().toString(), 100.0)),
                100.0,
                "SOUP",
                List.of("VEGAN")
        ));

        JsonNode body = readBody(response);
        assertEquals(404, response.statusCode());
        assertEquals("Not found", body.get("title").asText());
    }

    @Test
    @DisplayName("should derive dish category from macro in name")
    void shouldDeriveDishCategoryFromMacroInName() throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = post("/api/dishes", buildDishRequest(
                "!суп Борщ",
                List.of(),
                null,
                null,
                null,
                null,
                List.of(ingredient(productId, 100.0)),
                100.0,
                null,
                List.of("VEGAN")
        ));

        JsonNode body = readBody(response);
        assertEquals(201, response.statusCode());
        assertEquals("Борщ", body.get("name").asText());
        assertEquals("SOUP", body.get("category").asText());
    }

    @Test
    @DisplayName("should prefer explicit category over macro in name")
    void shouldPreferExplicitCategoryOverMacroInName() throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = post("/api/dishes", buildDishRequest(
                "!суп Борщ",
                List.of(),
                null,
                null,
                null,
                null,
                List.of(ingredient(productId, 100.0)),
                100.0,
                "SALAD",
                List.of("VEGAN")
        ));

        JsonNode body = readBody(response);
        assertEquals(201, response.statusCode());
        assertEquals("Борщ", body.get("name").asText());
        assertEquals("SALAD", body.get("category").asText());
    }
}
