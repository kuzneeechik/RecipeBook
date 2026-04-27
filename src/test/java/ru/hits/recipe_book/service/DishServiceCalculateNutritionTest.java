package ru.hits.recipe_book.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hits.recipe_book.dto.CalculatedNutritionResponse;
import ru.hits.recipe_book.dto.DishIngredientRequest;
import ru.hits.recipe_book.exception.EmptyIngredientsException;
import ru.hits.recipe_book.exception.NotFoundException;
import ru.hits.recipe_book.model.Product;
import ru.hits.recipe_book.repository.DishRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DishService.calculate")
public class DishServiceCalculateNutritionTest {

    @Mock
    private DishRepository dishRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private DishService dishService;

    @ParameterizedTest
    @CsvSource({
            "0.1, 0.12, 0.01, 0.01, 0.02",
            "50.0, 60.0, 5.0, 2.5, 10.0",
            "99.9, 119.88, 9.99, 5.0, 19.98",
            "100.0, 120.0, 10.0, 5.0, 20.0",
            "100.1, 120.12, 10.01, 5.0, 20.02",
            "150.0, 180.0, 15.0, 7.5, 30.0"
    })
    @DisplayName("nutritional value of a single-ingredient dish should be calculated proportionally")
    void shouldCalculateNutritionProportionallyForSingleIngredient(
            double grams,
            double expectedCalories,
            double expectedProteins,
            double expectedFats,
            double expectedCarbohydrates
    ) {
        UUID productId = UUID.randomUUID();

        var product = createProduct(120.0, 10.0, 5.0, 20.0);

        DishIngredientRequest ingredientRequest = new DishIngredientRequest(productId, grams);

        when(productService.findProduct(productId)).thenReturn(product);

        CalculatedNutritionResponse result = dishService.calculate(List.of(ingredientRequest));

        assertEquals(expectedCalories, result.calories(), 0.001);
        assertEquals(expectedProteins, result.proteins(), 0.001);
        assertEquals(expectedFats, result.fats(), 0.001);
        assertEquals(expectedCarbohydrates, result.carbohydrates(), 0.001);
    }

    @Test
    @DisplayName("should calculate total nutrition for a dish with multiple ingredients")
    void shouldCalculateTotalNutritionForMultipleIngredients() {
        UUID firstProductId = UUID.randomUUID();
        UUID secondProductId = UUID.randomUUID();

        var firstProduct = createProduct(120.0, 10.0, 5.0, 20.0);
        var secondProduct = createProduct(100.0, 7.0, 3.0, 15.0);

        DishIngredientRequest secondIngredientRequest = new DishIngredientRequest(secondProductId, 50.0);
        DishIngredientRequest firstIngredientRequest = new DishIngredientRequest(firstProductId, 100.0);

        when(productService.findProduct(secondProductId)).thenReturn(secondProduct);
        when(productService.findProduct(firstProductId)).thenReturn(firstProduct);

        CalculatedNutritionResponse result = dishService.calculate(
                List.of(firstIngredientRequest, secondIngredientRequest));

        assertEquals(170.0, result.calories(), 0.001);
        assertEquals(13.5, result.proteins(), 0.001);
        assertEquals(6.5, result.fats(), 0.001);
        assertEquals(27.5, result.carbohydrates(), 0.001);
    }

    @Test
    @DisplayName("should merge nutrition of duplicate ingredients")
    void shouldMergeNutritionOfDuplicateIngredients() {
        UUID productId = UUID.randomUUID();

        var product = createProduct(120.0, 10.0, 5.0, 20.0);

        DishIngredientRequest ingredientRequest = new DishIngredientRequest(productId, 100.0);

        when(productService.findProduct(productId)).thenReturn(product);

        CalculatedNutritionResponse result = dishService.calculate(
                List.of(ingredientRequest, ingredientRequest));

        assertEquals(240.0, result.calories(), 0.001);
        assertEquals(20.0, result.proteins(), 0.001);
        assertEquals(10.0, result.fats(), 0.001);
        assertEquals(40.0, result.carbohydrates(), 0.001);
    }

    @Test
    @DisplayName("should return zero nutrition for zero nutrition ingredient")
    void shouldReturnZeroNutritionForZeroNutritionIngredient() {
        UUID productId = UUID.randomUUID();

        var product = createProduct(0, 0, 0, 0);

        DishIngredientRequest ingredientRequest = new DishIngredientRequest(productId, 100.0);

        when(productService.findProduct(productId)).thenReturn(product);

        CalculatedNutritionResponse result = dishService.calculate(List.of(ingredientRequest));

        assertEquals(0, result.calories(), 0.001);
        assertEquals(0, result.proteins(), 0.001);
        assertEquals(0, result.fats(), 0.001);
        assertEquals(0, result.carbohydrates(), 0.001);
    }

    @Test
    @DisplayName("should round calculated nutrition to two decimals")
    void shouldRoundCalculatedNutritionToTwoDecimals() {
        UUID productId = UUID.randomUUID();

        var product = createProduct(120.13, 10.13, 5.13, 20.13);

        DishIngredientRequest ingredientRequest = new DishIngredientRequest(productId, 73.2);

        when(productService.findProduct(productId)).thenReturn(product);

        CalculatedNutritionResponse result = dishService.calculate(List.of(ingredientRequest));

        assertEquals(87.94, result.calories(), 0.001);
        assertEquals(7.42, result.proteins(), 0.001);
        assertEquals(3.76, result.fats(), 0.001);
        assertEquals(14.74, result.carbohydrates(), 0.001);
    }

    @Test
    @DisplayName("should throw EmptyIngredientsException when ingredient list is empty")
    void shouldThrowExceptionWhenIngredientListIsEmpty() {
        assertThrows(EmptyIngredientsException.class, () -> dishService.calculate(List.of()));
    }

    @Test
    @DisplayName("should throw NotFoundException when ingredient doesn't exist")
    void shouldThrowExceptionWhenIngredientDoesNotExist() {
        UUID productId = UUID.randomUUID();

        DishIngredientRequest ingredientRequest = new DishIngredientRequest(productId, 100.0);

        when(productService.findProduct(productId))
                .thenThrow(new NotFoundException("Product not found: " + productId));

        assertThrows(NotFoundException.class, () -> dishService.calculate(List.of(ingredientRequest)));
    }

    private Product createProduct(double calories, double proteins, double fats, double carbohydrates) {
        Product product = new Product();
        product.setCalories(calories);
        product.setProteins(proteins);
        product.setFats(fats);
        product.setCarbohydrates(carbohydrates);
        return product;
    }
}
