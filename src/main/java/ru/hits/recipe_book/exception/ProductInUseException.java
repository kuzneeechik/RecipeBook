package ru.hits.recipe_book.exception;

import java.util.List;

public class ProductInUseException extends RuntimeException {
    private final List<String> dishNames;

    public ProductInUseException(List<String> dishNames) {
        super("Product cannot be deleted because it is used in dishes");
        this.dishNames = dishNames;
    }

    public List<String> getDishNames() {
        return dishNames;
    }
}
