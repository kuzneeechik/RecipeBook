package ru.hits.recipe_book.api.dish;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.hits.recipe_book.api.ExternalApiTestSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Dish delete API")
public class DishDeleteTest extends ExternalApiTestSupport {

    @Test
    @DisplayName("should delete dish by valid id")
    void shouldDeleteDishByValidId() throws Exception {
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

        HttpResponse<String> deleteResponse = delete("/api/dishes/" + dishId);
        HttpResponse<String> getResponse = get("/api/dishes/" + dishId);

        assertEquals(204, deleteResponse.statusCode());
        assertEquals(404, getResponse.statusCode());
    }

    @Test
    @DisplayName("should return not found when deleting unknown dish")
    void shouldReturnNotFoundWhenDeletingUnknownDish() throws Exception {
        HttpResponse<String> response = delete("/api/dishes/" + UUID.randomUUID());
        assertEquals(404, response.statusCode());
    }
}
