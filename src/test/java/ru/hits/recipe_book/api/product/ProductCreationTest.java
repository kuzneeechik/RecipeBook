package ru.hits.recipe_book.api.product;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Product create API")
public class ProductCreationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("should create product with valid data")
    void shouldCreateProductWithValidData() throws Exception {
        String requestBody = buildProductRequest(
                "Свёкла",
                "[\"https://example.com/photo-1.jpg\"]",
                43.0,
                0.1,
                1.0,
                1.5
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
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

    @ParameterizedTest
    @CsvSource({
            "Як",
            "Мак"
    })
    @DisplayName("should create product with valid name length")
    void shouldCreateProductWithValidName(
            String name
    ) throws Exception {
        String requestBody = buildProductRequest(
                name,
                "[]",
                43.0,
                0.1,
                1.0,
                1.5
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name));
    }

    @Test
    @DisplayName("should return Bad Request when name length less than two")
    void shouldReturnBadRequestWhenNameLengthLessThanTwo() throws Exception {
        String requestBody = buildProductRequest(
                "Я",
                "[]",
                43.0,
                0.1,
                1.0,
                1.5
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation error"))
                .andExpect(jsonPath("$.detail").value("Request body validation failed"))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @ParameterizedTest
    @CsvSource({
            "0",
            "1",
            "4",
            "5"
    })
    @DisplayName("should create product with valid number of photos")
    void shouldCreateProductWithValidNumberOfPhotos(int numberOfPhotos) throws Exception {
        var expectedPhotos = buildExpectedPhotosList(numberOfPhotos);

        String requestBody = buildProductRequest(
                "Свёкла",
                buildPhotosList(numberOfPhotos),
                43.0,
                0.1,
                1.0,
                1.5
        );

        mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.photos").isArray())
                .andExpect(jsonPath("$.photos.length()").value(numberOfPhotos))
                .andExpect(jsonPath("$.photos").value(expectedPhotos));
    }

    @Test
    @DisplayName("should return Bad Request when number of photos more than five")
    void shouldReturnBadRequestWhenNumberOfPhotosMoreThanFive() throws Exception {
        String requestBody = buildProductRequest(
                "Свёкла",
                buildPhotosList(6),
                43.0,
                0.1,
                1.0,
                1.5
        );

        mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation error"))
                .andExpect(jsonPath("$.detail").value("Request body validation failed"))
                .andExpect(jsonPath("$.errors.photos").exists());
    }

    @ParameterizedTest
    @CsvSource({
            "0",
            "0.1"
    })
    @DisplayName("should create product with valid number of calories")
    void shouldCreateProductWithValidNumberOfCalories(double numberOfCalories) throws Exception {
        String requestBody = buildProductRequest(
                "Свёкла",
                "[]",
                numberOfCalories,
                0.1,
                1.0,
                1.5
        );

        mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.calories").value(numberOfCalories));
    }

    @Test
    @DisplayName("should return Bad Request when number of calories less than zero")
    void shouldReturnBadRequestWhenNumberOfCaloriesLessThanZero() throws Exception {
        String requestBody = buildProductRequest(
                "Свёкла",
                "[]",
                -1,
                0.1,
                1.0,
                1.5
        );

        mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation error"))
                .andExpect(jsonPath("$.detail").value("Request body validation failed"))
                .andExpect(jsonPath("$.errors.calories").exists());
    }

    @ParameterizedTest
    @CsvSource({
            "0",
            "0.1",
            "99.9",
            "100"
    })
    @DisplayName("should create product with valid number of proteins")
    void shouldCreateProductWithValidNumberOfProteins(double numberOfProteins) throws Exception {
        String requestBody = buildProductRequest(
                "Свёкла",
                "[]",
                43.0,
                numberOfProteins,
                0,
                0
        );;

        mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.proteins").value(numberOfProteins));
    }

    @ParameterizedTest
    @CsvSource({
            "-1",
            "100.1"
    })
    @DisplayName("should return Bad Request when number of proteins less than zero or more than one hundred")
    void shouldReturnBadRequestWhenNumberOfProteinsLessThanZeroOrMoreThanOneHundred(
            double numberOfProteins
    ) throws Exception {
        String requestBody = buildProductRequest(
                "Свёкла",
                "[]",
                43.0,
                numberOfProteins,
                1.0,
                1.5
        );;

        mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation error"))
                .andExpect(jsonPath("$.detail").value("Request body validation failed"))
                .andExpect(jsonPath("$.errors.proteins").exists());
    }

    @ParameterizedTest
    @CsvSource({
            "0",
            "0.1",
            "99.9",
            "100"
    })
    @DisplayName("should create product with valid number of fats")
    void shouldCreateProductWithValidNumberOfFats(double numberOfFats) throws Exception {
        String requestBody = buildProductRequest(
                "Свёкла",
                "[]",
                43.0,
                0,
                numberOfFats,
                0
        );

        mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fats").value(numberOfFats));
    }

    @ParameterizedTest
    @CsvSource({
            "-1",
            "100.1"
    })
    @DisplayName("should return Bad Request when number of fats less than zero or more than one hundred")
    void shouldReturnBadRequestWhenNumberOfFatsLessThanZeroOrMoreThanOneHundred(
            double numberOfFats
    ) throws Exception {
        String requestBody = buildProductRequest(
                "Свёкла",
                "[]",
                43.0,
                0.1,
                numberOfFats,
                1.5
        );

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation error"))
                .andExpect(jsonPath("$.detail").value("Request body validation failed"))
                .andExpect(jsonPath("$.errors.fats").exists());
    }

    @ParameterizedTest
    @CsvSource({
            "0",
            "0.1",
            "99.9",
            "100"
    })
    @DisplayName("should create product with valid number of carbs")
    void shouldCreateProductWithValidNumberOfCarbs(double numberOfCarbs) throws Exception {
        String requestBody = buildProductRequest(
                "Свёкла",
                "[]",
                43.0,
                0,
                0,
                numberOfCarbs
        );

        mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.carbohydrates").value(numberOfCarbs));
    }

    @ParameterizedTest
    @CsvSource({
            "-1",
            "100.1"
    })
    @DisplayName("should return Bad Request when number of carbs less than zero or more than one hundred")
    void shouldReturnBadRequestWhenNumberOfCarbsLessThanZeroOrMoreThanOneHundred(
            double numberOfCarbs
    ) throws Exception {
        String requestBody = buildProductRequest(
                "Свёкла",
                "[]",
                43.0,
                0.1,
                1.0,
                numberOfCarbs
        );

        mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation error"))
                .andExpect(jsonPath("$.detail").value("Request body validation failed"))
                .andExpect(jsonPath("$.errors.carbohydrates").exists());
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0, 0",
            "0.05, 0.04, 0.01",
            "33.3, 33.3, 33.3",
            "40, 30, 30"
    })
    @DisplayName("should create product with valid nutrition sum")
    void shouldCreateProductWithValidNutritionSum(
            double proteins,
            double fats,
            double carbs
    ) throws Exception {
        String requestBody = buildProductRequest(
                "Свёкла",
                "[]",
                43.0,
                proteins,
                fats,
                carbs
        );;

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("should return Bad Request when nutrition sum more than one hundred")
    void shouldReturnBadRequestWhenNutritionSumMoreThanOneHundred() throws Exception {
        String requestBody = buildProductRequest(
                "Свёкла",
                "[]",
                43.0,
                50.0,
                50.0,
                50.0
        );

        mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation error"))
                .andExpect(jsonPath("$.detail")
                        .value("Proteins, fats and carbohydrates per 100g cannot exceed 100g in total"));
    }

    private String buildProductRequest(
            String name,
            String photos,
            double calories,
            double proteins,
            double fats,
            double carbohydrates
    ) {
        return """
            {
              "name": "%s",
              "photos": %s,
              "calories": %s,
              "proteins": %s,
              "fats": %s,
              "carbohydrates": %s,
              "composition": "Единица продукта",
              "category": "VEGETABLES",
              "cookingRequirement": "REQUIRES_COOKING",
              "flags": ["VEGAN", "GLUTEN_FREE", "SUGAR_FREE"]
            }
            """.formatted(
                name,
                photos,
                calories,
                proteins,
                fats,
                carbohydrates
        );
    }

    private String buildPhotosList(int number) {
        var photos = new ArrayList<String>();

        for (int i = 0; i < number; i++) {
            photos.add("\"https://example.com/photo-" + i + ".jpg\"");
        }

        return "[" + String.join(", ", photos) + "]";
    }

    private List<String> buildExpectedPhotosList(int number) {
        var photos = new ArrayList<String>();

        for (int i = 0; i < number; i++) {
            photos.add("https://example.com/photo-" + i + ".jpg");
        }

        return photos;
    }
}
