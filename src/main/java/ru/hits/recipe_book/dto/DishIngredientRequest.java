package ru.hits.recipe_book.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record DishIngredientRequest(
        @NotNull UUID productId,
        @DecimalMin(value = "0.0", inclusive = false) double quantityGrams
) {
}
