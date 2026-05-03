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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Product get all with filtering and sorting API")
public class ProductGetAllTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("should filter products by category")
    void shouldFilterProductsByCategory() throws Exception {
        createProducts();

        mockMvc.perform(get("/api/products")
                .param("category", "VEGETABLES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Свёкла"))
                .andExpect(jsonPath("$[0].category").value("VEGETABLES"));
    }

    @Test
    @DisplayName("should filter products by cooking requirement")
    void shouldFilterProductsByCookingRequirement() throws Exception {
        createProducts();

        mockMvc.perform(get("/api/products")
                    .param("cookingRequirement", "READY_TO_EAT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Свинина"))
                .andExpect(jsonPath("$[0].cookingRequirement").value("READY_TO_EAT"));
    }

    @Test
    @DisplayName("should filter products by diet flag")
    void shouldFilterProductsByDietFlag() throws Exception {
        createProducts();

        mockMvc.perform(get("/api/products")
                    .param("flags", "VEGAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Свёкла"))
                .andExpect(jsonPath("$[0].flags[0]").value("VEGAN"));
    }

    @Test
    @DisplayName("should return Bad Request when get invalid filter field")
    void shouldReturnBadRequestWhenGetInvalidFilterField() throws Exception {
        createProducts();

        mockMvc.perform(get("/api/products")
                        .param("cookingRequirement", "INVALID_FLAG"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should search products by name ignoring case")
    void shouldSearchProductsByNameIgnoringCase() throws Exception {
        createProducts();

        mockMvc.perform(get("/api/products")
                    .param("search", "свин"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("should sort products by calories descending")
    void shouldSortProductsByCaloriesDescending() throws Exception {
        createProducts();

        mockMvc.perform(get("/api/products")
                    .param("sortBy", "calories")
                    .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Свинина"))
                .andExpect(jsonPath("$[1].name").value("Свёкла"));
    }

    @Test
    @DisplayName("should return Bad Request when sorting by invalid field")
    void shouldReturnBadRequestWhenSortingByInvalidField() throws Exception {
        createProducts();

        mockMvc.perform(get("/api/products")
                    .param("sortBy", "invalid")
                    .param("direction", "asc"))
                .andExpect(status().isBadRequest());
    }

    private void createProducts() throws Exception {
        String firstRequestBody = buildProductRequest(
                "Свёкла",
                43.0,
                0.1,
                1.0,
                1.5,
                "VEGETABLES",
                "REQUIRES_COOKING",
                "VEGAN"
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstRequestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String secondRequestBody = buildProductRequest(
                "Свинина",
                110.0,
                20.0,
                10.0,
                11.5,
                "MEAT",
                "READY_TO_EAT",
                "SUGAR_FREE"
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondRequestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private String buildProductRequest(
            String name,
            double calories,
            double proteins,
            double fats,
            double carbohydrates,
            String category,
            String cookingRequirement,
            String flags
    ) {
        return """
            {
              "name": "%s",
              "photos": [],
              "calories": "%s",
              "proteins": "%s",
              "fats": "%s",
              "carbohydrates": "%s",
              "composition": "Единица продукта",
              "category": "%s",
              "cookingRequirement": "%s",
              "flags": ["%s"]
            }
            """.formatted(
                name,
                calories,
                proteins,
                fats,
                carbohydrates,
                category,
                cookingRequirement,
                flags
        );
    }
}
