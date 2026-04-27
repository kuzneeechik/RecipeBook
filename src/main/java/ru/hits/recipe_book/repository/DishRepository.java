package ru.hits.recipe_book.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.hits.recipe_book.model.Dish;

public interface DishRepository extends JpaRepository<Dish, UUID> {
    @EntityGraph(attributePaths = {"ingredients", "ingredients.product"})
    List<Dish> findByIngredientsProductId(UUID productId);
}
