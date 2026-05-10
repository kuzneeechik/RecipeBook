package ru.hits.recipe_book.api.product;

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

@DisplayName("Product update API")
public class ProductUpdateTest extends ExternalApiTestSupport {

    @Test
    @DisplayName("should update product with valid data")
    void shouldUpdateProductWithValidData() throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = put("/api/products/" + productId, buildProductRequest(
                "Свёкла",
                List.of("https://example.com/photo-1.jpg"),
                43.0,
                0.1,
                1.0,
                1.5,
                "Единица продукта",
                "VEGETABLES",
                "REQUIRES_COOKING",
                List.of("VEGAN", "GLUTEN_FREE", "SUGAR_FREE")
        ));

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
    @DisplayName("should return not found when updating unknown product")
    void shouldReturnNotFoundWhenUpdatingUnknownProduct() throws Exception {
        HttpResponse<String> response = put("/api/products/" + UUID.randomUUID(), buildProductRequest(
                "Свёкла",
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

        assertEquals(404, response.statusCode());
    }

    @ParameterizedTest
    @CsvSource({
            "Як",
            "Мак"
    })
    @DisplayName("should update product with valid name length")
    void shouldUpdateProductWithValidName(String name) throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = put("/api/products/" + productId, buildProductRequest(
                name,
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

        JsonNode body = readBody(response);

        assertEquals(200, response.statusCode());
        assertEquals(productId, body.get("id").asText());
        assertEquals(name, body.get("name").asText());
    }

    @Test
    @DisplayName("should return bad request when name length less than two")
    void shouldReturnBadRequestWhenNameLengthLessThanTwo() throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = put("/api/products/" + productId, buildProductRequest(
                "Я",
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
    @DisplayName("should update product with valid number of photos")
    void shouldUpdateProductWithValidNumberOfPhotos(int numberOfPhotos) throws Exception {
        String productId = createProductAndReturnId();
        List<String> expectedPhotos = buildPhotosList(numberOfPhotos);

        HttpResponse<String> response = put("/api/products/" + productId, buildProductRequest(
                "Свёкла",
                expectedPhotos,
                43.0,
                0.1,
                1.0,
                1.5,
                "Единица продукта",
                "VEGETABLES",
                "REQUIRES_COOKING",
                List.of("VEGAN")
        ));

        JsonNode body = readBody(response);

        assertEquals(200, response.statusCode());
        assertEquals(numberOfPhotos, body.get("photos").size());
        assertContainsExactly(body.get("photos"), expectedPhotos);
    }

    @Test
    @DisplayName("should return bad request when number of photos more than five")
    void shouldReturnBadRequestWhenNumberOfPhotosMoreThanFive() throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = put("/api/products/" + productId, buildProductRequest(
                "Свёкла",
                buildPhotosList(6),
                43.0,
                0.1,
                1.0,
                1.5,
                "Единица продукта",
                "VEGETABLES",
                "REQUIRES_COOKING",
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
            "0",
            "0.1"
    })
    @DisplayName("should update product with valid number of calories")
    void shouldUpdateProductWithValidNumberOfCalories(double numberOfCalories) throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = put("/api/products/" + productId, buildProductRequest(
                "Свёкла",
                List.of(),
                numberOfCalories,
                0.1,
                1.0,
                1.5,
                "Единица продукта",
                "VEGETABLES",
                "REQUIRES_COOKING",
                List.of("VEGAN")
        ));

        JsonNode body = readBody(response);

        assertEquals(200, response.statusCode());
        assertEquals(numberOfCalories, body.get("calories").asDouble());
    }

    @Test
    @DisplayName("should return bad request when number of calories less than zero")
    void shouldReturnBadRequestWhenNumberOfCaloriesLessThanZero() throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = put("/api/products/" + productId, buildProductRequest(
                "Свёкла",
                List.of(),
                -1.0,
                0.1,
                1.0,
                1.5,
                "Единица продукта",
                "VEGETABLES",
                "REQUIRES_COOKING",
                List.of("VEGAN")
        ));

        JsonNode body = readBody(response);

        assertEquals(400, response.statusCode());
        assertEquals("Validation error", body.get("title").asText());
        assertEquals("Request body validation failed", body.get("detail").asText());
        assertTrue(body.get("errors").has("calories"));
    }

    @ParameterizedTest
    @CsvSource({
            "0",
            "0.1",
            "99.9",
            "100"
    })
    @DisplayName("should update product with valid number of proteins")
    void shouldUpdateProductWithValidNumberOfProteins(double numberOfProteins) throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = put("/api/products/" + productId, buildProductRequest(
                "Свёкла",
                List.of(),
                43.0,
                numberOfProteins,
                0.0,
                0.0,
                "Единица продукта",
                "VEGETABLES",
                "REQUIRES_COOKING",
                List.of("VEGAN")
        ));

        JsonNode body = readBody(response);

        assertEquals(200, response.statusCode());
        assertEquals(numberOfProteins, body.get("proteins").asDouble());
    }

    @ParameterizedTest
    @CsvSource({
            "-1",
            "100.1"
    })
    @DisplayName("should return bad request when number of proteins less than zero or more than one hundred")
    void shouldReturnBadRequestWhenNumberOfProteinsLessThanZeroOrMoreThanOneHundred(double numberOfProteins) throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = put("/api/products/" + productId, buildProductRequest(
                "Свёкла",
                List.of(),
                43.0,
                numberOfProteins,
                1.0,
                1.5,
                "Единица продукта",
                "VEGETABLES",
                "REQUIRES_COOKING",
                List.of("VEGAN")
        ));

        JsonNode body = readBody(response);

        assertEquals(400, response.statusCode());
        assertEquals("Validation error", body.get("title").asText());
        assertEquals("Request body validation failed", body.get("detail").asText());
        assertTrue(body.get("errors").has("proteins"));
    }

    @ParameterizedTest
    @CsvSource({
            "0",
            "0.1",
            "99.9",
            "100"
    })
    @DisplayName("should update product with valid number of fats")
    void shouldUpdateProductWithValidNumberOfFats(double numberOfFats) throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = put("/api/products/" + productId, buildProductRequest(
                "Свёкла",
                List.of(),
                43.0,
                0.0,
                numberOfFats,
                0.0,
                "Единица продукта",
                "VEGETABLES",
                "REQUIRES_COOKING",
                List.of("VEGAN")
        ));

        JsonNode body = readBody(response);

        assertEquals(200, response.statusCode());
        assertEquals(numberOfFats, body.get("fats").asDouble());
    }

    @ParameterizedTest
    @CsvSource({
            "-1",
            "100.1"
    })
    @DisplayName("should return bad request when number of fats less than zero or more than one hundred")
    void shouldReturnBadRequestWhenNumberOfFatsLessThanZeroOrMoreThanOneHundred(double numberOfFats) throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = put("/api/products/" + productId, buildProductRequest(
                "Свёкла",
                List.of(),
                43.0,
                0.1,
                numberOfFats,
                1.5,
                "Единица продукта",
                "VEGETABLES",
                "REQUIRES_COOKING",
                List.of("VEGAN")
        ));

        JsonNode body = readBody(response);

        assertEquals(400, response.statusCode());
        assertEquals("Validation error", body.get("title").asText());
        assertEquals("Request body validation failed", body.get("detail").asText());
        assertTrue(body.get("errors").has("fats"));
    }

    @ParameterizedTest
    @CsvSource({
            "0",
            "0.1",
            "99.9",
            "100"
    })
    @DisplayName("should update product with valid number of carbs")
    void shouldUpdateProductWithValidNumberOfCarbs(double numberOfCarbs) throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = put("/api/products/" + productId, buildProductRequest(
                "Свёкла",
                List.of(),
                43.0,
                0.0,
                0.0,
                numberOfCarbs,
                "Единица продукта",
                "VEGETABLES",
                "REQUIRES_COOKING",
                List.of("VEGAN")
        ));

        JsonNode body = readBody(response);

        assertEquals(200, response.statusCode());
        assertEquals(numberOfCarbs, body.get("carbohydrates").asDouble());
    }

    @ParameterizedTest
    @CsvSource({
            "-1",
            "100.1"
    })
    @DisplayName("should return bad request when number of carbs less than zero or more than one hundred")
    void shouldReturnBadRequestWhenNumberOfCarbsLessThanZeroOrMoreThanOneHundred(double numberOfCarbs) throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = put("/api/products/" + productId, buildProductRequest(
                "Свёкла",
                List.of(),
                43.0,
                0.1,
                1.0,
                numberOfCarbs,
                "Единица продукта",
                "VEGETABLES",
                "REQUIRES_COOKING",
                List.of("VEGAN")
        ));

        JsonNode body = readBody(response);

        assertEquals(400, response.statusCode());
        assertEquals("Validation error", body.get("title").asText());
        assertEquals("Request body validation failed", body.get("detail").asText());
        assertTrue(body.get("errors").has("carbohydrates"));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0, 0",
            "0.05, 0.04, 0.01",
            "33.3, 33.3, 33.3",
            "40, 30, 30"
    })
    @DisplayName("should update product with valid nutrition sum")
    void shouldUpdateProductWithValidNutritionSum(double proteins, double fats, double carbs) throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = put("/api/products/" + productId, buildProductRequest(
                "Свёкла",
                List.of(),
                43.0,
                proteins,
                fats,
                carbs,
                "Единица продукта",
                "VEGETABLES",
                "REQUIRES_COOKING",
                List.of("VEGAN")
        ));

        JsonNode body = readBody(response);

        assertEquals(200, response.statusCode());
        assertEquals(productId, body.get("id").asText());
    }

    @Test
    @DisplayName("should return bad request when nutrition sum more than one hundred")
    void shouldReturnBadRequestWhenNutritionSumMoreThanOneHundred() throws Exception {
        String productId = createProductAndReturnId();

        HttpResponse<String> response = put("/api/products/" + productId, buildProductRequest(
                "Свёкла",
                List.of(),
                43.0,
                50.0,
                50.0,
                50.0,
                "Единица продукта",
                "VEGETABLES",
                "REQUIRES_COOKING",
                List.of("VEGAN")
        ));

        JsonNode body = readBody(response);

        assertEquals(400, response.statusCode());
        assertEquals("Validation error", body.get("title").asText());
        assertEquals("Proteins, fats and carbohydrates per 100g cannot exceed 100g in total", body.get("detail").asText());
    }
}
