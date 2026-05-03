package ru.hits.recipe_book.api.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Product delete API")
public class ProductDeleteTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("should delete product not used in dish by valid id")
    void shouldDeleteProductWhenItIsNotUsedInDish() throws Exception {
        var productId = createProductAndReturnId();

        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("should return Conflict when deleting product used in dish")
    void shouldReturnConflictWhenDeletingProductUsedInDish() throws Exception {
        var productId = createProductAndReturnId();

        createDishWithProduct(productId);

        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Product is used in dishes"))
                .andExpect(jsonPath("$.dishes").isArray())
                .andExpect(jsonPath("$.dishes[0]").value("Борщ"));
    }

    @Test
    @DisplayName("should return Not Found when deleting product by unknown id")
    void shouldReturnNotFoundWhenDeletingProductByUnknownId() throws Exception {
        var id = UUID.randomUUID().toString();

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNotFound());
    }

    private String createProductAndReturnId() throws Exception {
        String requestBody = """
            {
              "name": "Свёкла",
              "photos": ["https://example.com/photo-1.jpg"],
              "calories": 43.0,
              "proteins": 0.1,
              "fats": 1.0,
              "carbohydrates": 1.5,
              "composition": "Единица продукта",
              "category": "VEGETABLES",
              "cookingRequirement": "REQUIRES_COOKING",
              "flags": ["VEGAN", "GLUTEN_FREE", "SUGAR_FREE"]
            }
            """;

        String response = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    private void createDishWithProduct(String productId) throws Exception {
        String requestBody = """
                {
                    "name": "Борщ",
                    "photos": ["https://example.com/photo-1.jpg"],
                    "calories": 43.0,
                    "proteins": 0.1,
                    "fats": 1.0,
                    "carbohydrates": 1.5,
                    "ingredients": [
                       {
                         "productId": "%s",
                         "quantityGrams": 100.0
                       }
                    ],
                    "servingSizeGrams": 400,
                    "category": "SOUP",
                    "flags": ["VEGAN"]
                }
                """.formatted(productId);

        mockMvc.perform(post("/api/dishes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isCreated());
    }
}
