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

@DisplayName("Dish calculate API")
public class DishCalculateTest extends ExternalApiTestSupport {

    @ParameterizedTest
    @CsvSource({
            "0.01, 0.01, 0.0, 0.0, 0.0",
            "99.99, 119.99, 10.0, 5.0, 20.0",
            "100.0, 120.0, 10.0, 5.0, 20.0",
            "100.01, 120.01, 10.0, 5.0, 20.0"
    })
    @DisplayName("should calculate nutrition for single ingredient boundary values")
    void shouldCalculateNutritionForSingleIngredientBoundaryValues(
            double quantityGrams,
            double expectedCalories,
            double expectedProteins,
            double expectedFats,
            double expectedCarbohydrates
    ) throws Exception {
        String productId = createProductAndReturnId(
                "Тофу",
                120.0,
                10.0,
                5.0,
                20.0,
                "VEGETABLES",
                "READY_TO_EAT",
                List.of("VEGAN")
        );

        HttpResponse<String> response = post("/api/dishes/calculate", objectMapper.writeValueAsString(List.of(
                ingredient(productId, quantityGrams)
        )));

        JsonNode body = readBody(response);
        assertEquals(200, response.statusCode());
        assertEquals(expectedCalories, body.get("calories").asDouble());
        assertEquals(expectedProteins, body.get("proteins").asDouble());
        assertEquals(expectedFats, body.get("fats").asDouble());
        assertEquals(expectedCarbohydrates, body.get("carbohydrates").asDouble());
    }

    @Test
    @DisplayName("should calculate nutrition for multiple ingredients")
    void shouldCalculateNutritionForMultipleIngredients() throws Exception {
        String firstProductId = createProductAndReturnId(
                "Тофу",
                120.0,
                10.0,
                5.0,
                20.0,
                "VEGETABLES",
                "READY_TO_EAT",
                List.of("VEGAN")
        );
        String secondProductId = createProductAndReturnId(
                "Грибы",
                40.0,
                4.0,
                1.0,
                3.0,
                "VEGETABLES",
                "READY_TO_EAT",
                List.of("VEGAN")
        );

        HttpResponse<String> response = post("/api/dishes/calculate", objectMapper.writeValueAsString(List.of(
                ingredient(firstProductId, 100.0),
                ingredient(secondProductId, 50.0)
        )));

        JsonNode body = readBody(response);
        assertEquals(200, response.statusCode());
        assertEquals(140.0, body.get("calories").asDouble());
        assertEquals(12.0, body.get("proteins").asDouble());
        assertEquals(5.5, body.get("fats").asDouble());
        assertEquals(21.5, body.get("carbohydrates").asDouble());
    }

    @Test
    @DisplayName("should merge duplicate ingredient products when calculating nutrition")
    void shouldMergeDuplicateIngredientProductsWhenCalculatingNutrition() throws Exception {
        String productId = createProductAndReturnId(
                "Тофу",
                120.0,
                10.0,
                5.0,
                20.0,
                "VEGETABLES",
                "READY_TO_EAT",
                List.of("VEGAN")
        );

        HttpResponse<String> response = post("/api/dishes/calculate", objectMapper.writeValueAsString(List.of(
                ingredient(productId, 50.0),
                ingredient(productId, 100.0)
        )));

        JsonNode body = readBody(response);
        assertEquals(200, response.statusCode());
        assertEquals(180.0, body.get("calories").asDouble());
        assertEquals(15.0, body.get("proteins").asDouble());
        assertEquals(7.5, body.get("fats").asDouble());
        assertEquals(30.0, body.get("carbohydrates").asDouble());
    }

    @Test
    @DisplayName("should return bad request when ingredients list is empty for calculation")
    void shouldReturnBadRequestWhenIngredientsListIsEmptyForCalculation() throws Exception {
        HttpResponse<String> response = post("/api/dishes/calculate", objectMapper.writeValueAsString(List.of()));
        JsonNode body = readBody(response);

        assertEquals(400, response.statusCode());
        assertEquals("Validation error", body.get("title").asText());
        assertEquals("Dish ingredients list cannot be empty", body.get("detail").asText());
    }

    @Test
    @DisplayName("should return bad request when ingredient quantity is zero for calculation")
    void shouldReturnBadRequestWhenIngredientQuantityIsZeroForCalculation() throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = post("/api/dishes/calculate", objectMapper.writeValueAsString(List.of(
                ingredient(productId, 0.0)
        )));
        JsonNode body = readBody(response);

        assertEquals(400, response.statusCode());
        assertTrue(
                response.body().contains("quantityGrams")
                        || response.body().contains("Validation error")
                        || response.body().contains("Bad Request"),
                "Expected validation response for invalid ingredient quantity, but got: " + response.body()
        );
    }

    @Test
    @DisplayName("should return not found when calculating nutrition for unknown product")
    void shouldReturnNotFoundWhenCalculatingNutritionForUnknownProduct() throws Exception {
        HttpResponse<String> response = post("/api/dishes/calculate", objectMapper.writeValueAsString(List.of(
                ingredient(UUID.randomUUID().toString(), 100.0)
        )));
        JsonNode body = readBody(response);

        assertEquals(404, response.statusCode());
        assertEquals("Not found", body.get("title").asText());
    }
}
