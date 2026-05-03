package ru.hits.recipe_book.api.product;

import com.fasterxml.jackson.databind.JsonNode;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Product get by id API")
public class ProductGettingTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("should return product by valid id")
    void shouldReturnProductById() throws Exception {
        var product = createProduct();
        var productId = product.get("id").asText();

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Свёкла"))
                .andExpect(jsonPath("$.photos").isArray())
                .andExpect(jsonPath("$.photos.length()").value(1))
                .andExpect(jsonPath("$.photos[0]").value("https://example.com/photo-1.jpg"))
                .andExpect(jsonPath("$.calories").value(43.0))
                .andExpect(jsonPath("$.proteins").value(0.1))
                .andExpect(jsonPath("$.fats").value(1.0))
                .andExpect(jsonPath("$.carbohydrates").value(1.5))
                .andExpect(jsonPath("$.composition").value("Единица продукта"))
                .andExpect(jsonPath("$.category").value("VEGETABLES"))
                .andExpect(jsonPath("$.cookingRequirement").value("REQUIRES_COOKING"))
                .andExpect(jsonPath("$.flags").isArray())
                .andExpect(jsonPath("$.flags.length()").value(3))
                .andExpect(jsonPath("$.flags").value(org.hamcrest.Matchers.containsInAnyOrder(
                                "VEGAN",
                                "GLUTEN_FREE",
                                "SUGAR_FREE"
                        )));
    }

    @Test
    @DisplayName("should return Not Found when getting unknown product")
    void shouldReturnNotFoundWhenGettingProductByUnknownId() throws Exception {
        var productId = UUID.randomUUID().toString();

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isNotFound());
    }

    private JsonNode createProduct() throws Exception {
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

        return objectMapper.readTree(response);
    }
}
