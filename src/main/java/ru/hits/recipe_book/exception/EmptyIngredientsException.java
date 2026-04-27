package ru.hits.recipe_book.exception;

public class EmptyIngredientsException extends ValidationException {
    public EmptyIngredientsException() {
        super("Dish ingredients list cannot be empty");
    }
}
