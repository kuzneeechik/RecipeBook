package ru.hits.recipe_book.dto;

public record CalculatedNutritionResponse(
        double calories,
        double proteins,
        double fats,
        double carbohydrates
) {
}
