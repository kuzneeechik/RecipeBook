package ru.hits.recipe_book.api.dish;

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

@DisplayName("Dish get all API")
public class DishGetAllTest extends ExternalApiTestSupport {

    private String scopeToken;
    private String soupName;
    private String saladName;

    @BeforeEach
    void createDishes() throws Exception {
        scopeToken = "dish-" + UUID.randomUUID().toString().substring(0, 8);
        soupName = "Борщ " + scopeToken;
        saladName = "Салат " + scopeToken;

        String veganProductId = createProductAndReturnId(
                "Тофу " + scopeToken,
                100.0,
                10.0,
                5.0,
                20.0,
                "VEGETABLES",
                "READY_TO_EAT",
                List.of("VEGAN", "GLUTEN_FREE")
        );
        String neutralProductId = createProductAndReturnId(
                "Курица " + scopeToken,
                200.0,
                20.0,
                10.0,
                0.0,
                "MEAT",
                "READY_TO_EAT",
                List.of("SUGAR_FREE")
        );

        createDishAndReturnId(
                soupName,
                null,
                null,
                null,
                null,
                List.of(ingredient(neutralProductId, 100.0)),
                100.0,
                "SOUP",
                List.of("SUGAR_FREE")
        );
        createDishAndReturnId(
                saladName,
                null,
                null,
                null,
                null,
                List.of(ingredient(veganProductId, 100.0)),
                100.0,
                "SALAD",
                List.of("VEGAN", "GLUTEN_FREE")
        );
    }

    @Test
    @DisplayName("should filter dishes by category")
    void shouldFilterDishesByCategory() throws Exception {
        HttpResponse<String> response = get("/api/dishes?category=SALAD&search=" + encode(scopeToken));
        JsonNode body = readBody(response);

        assertEquals(200, response.statusCode());
        assertEquals(1, body.size());
        assertEquals(saladName, body.get(0).get("name").asText());
        assertEquals("SALAD", body.get(0).get("category").asText());
    }

    @Test
    @DisplayName("should filter dishes by flag")
    void shouldFilterDishesByFlag() throws Exception {
        HttpResponse<String> response = get("/api/dishes?flags=VEGAN&search=" + encode(scopeToken));
        JsonNode body = readBody(response);

        assertEquals(200, response.statusCode());
        assertEquals(1, body.size());
        assertEquals(saladName, body.get(0).get("name").asText());
        assertContainsInAnyOrder(body.get(0).get("flags"), List.of("VEGAN", "GLUTEN_FREE"));
    }

    @Test
    @DisplayName("should search dishes by name ignoring case")
    void shouldSearchDishesByNameIgnoringCase() throws Exception {
        HttpResponse<String> response = get("/api/dishes?search=" + encode(soupName.toLowerCase()));
        JsonNode body = readBody(response);

        assertEquals(200, response.statusCode());
        assertEquals(1, body.size());
        assertEquals(soupName, body.get(0).get("name").asText());
    }

    @Test
    @DisplayName("should return dishes sorted by name")
    void shouldReturnDishesSortedByName() throws Exception {
        HttpResponse<String> response = get("/api/dishes?search=" + encode(scopeToken));
        JsonNode body = readBody(response);

        assertEquals(200, response.statusCode());
        assertEquals(2, body.size());
        assertEquals(soupName, body.get(0).get("name").asText());
        assertEquals(saladName, body.get(1).get("name").asText());
    }

    @Test
    @DisplayName("should return bad request when category filter is invalid")
    void shouldReturnBadRequestWhenCategoryFilterIsInvalid() throws Exception {
        HttpResponse<String> response = get("/api/dishes?category=INVALID&search=" + encode(scopeToken));
        assertEquals(400, response.statusCode());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
