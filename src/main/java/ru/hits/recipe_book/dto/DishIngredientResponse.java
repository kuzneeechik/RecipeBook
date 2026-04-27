package ru.hits.recipe_book.dto;

import java.util.UUID;

public record DishIngredientResponse(
        UUID productId,
        String productName,
        double quantityGrams
) {
}
