package ru.hits.recipe_book.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import ru.hits.recipe_book.model.CookingRequirement;
import ru.hits.recipe_book.model.DietFlag;
import ru.hits.recipe_book.model.ProductCategory;

public record ProductResponse(
        UUID id,
        String name,
        List<String> photos,
        double calories,
        double proteins,
        double fats,
        double carbohydrates,
        String composition,
        ProductCategory category,
        CookingRequirement cookingRequirement,
        Set<DietFlag> flags,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
