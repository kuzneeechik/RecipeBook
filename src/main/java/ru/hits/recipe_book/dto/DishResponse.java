package ru.hits.recipe_book.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import ru.hits.recipe_book.model.DietFlag;
import ru.hits.recipe_book.model.DishCategory;

public record DishResponse(
        UUID id,
        String name,
        List<String> photos,
        double calories,
        double proteins,
        double fats,
        double carbohydrates,
        List<DishIngredientResponse> ingredients,
        double servingSizeGrams,
        DishCategory category,
        Set<DietFlag> flags,
        Set<DietFlag> availableFlags,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
