package ru.hits.recipe_book.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import ru.hits.recipe_book.model.CookingRequirement;
import ru.hits.recipe_book.model.DietFlag;
import ru.hits.recipe_book.model.ProductCategory;

public record ProductRequest(
        @NotBlank @Size(min = 2) String name,
        @Size(max = 5) List<@NotBlank String> photos,
        @DecimalMin("0.0") double calories,
        @DecimalMin("0.0") @DecimalMax("100.0") double proteins,
        @DecimalMin("0.0") @DecimalMax("100.0") double fats,
        @DecimalMin("0.0") @DecimalMax("100.0") double carbohydrates,
        String composition,
        @NotNull ProductCategory category,
        @NotNull CookingRequirement cookingRequirement,
        Set<DietFlag> flags
) {
}
