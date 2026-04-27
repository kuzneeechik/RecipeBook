package ru.hits.recipe_book.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import ru.hits.recipe_book.model.DietFlag;
import ru.hits.recipe_book.model.DishCategory;

public record DishRequest(
        @NotBlank @Size(min = 2) String name,
        @Size(max = 5) List<@NotBlank String> photos,
        @DecimalMin("0.0") Double calories,
        @DecimalMin("0.0") Double proteins,
        @DecimalMin("0.0") Double fats,
        @DecimalMin("0.0") Double carbohydrates,
        @NotEmpty List<@Valid DishIngredientRequest> ingredients,
        @DecimalMin(value = "0.0", inclusive = false) double servingSizeGrams,
        DishCategory category,
        Set<DietFlag> flags
) {
}
