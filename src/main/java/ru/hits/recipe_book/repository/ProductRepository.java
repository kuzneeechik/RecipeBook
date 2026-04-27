package ru.hits.recipe_book.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.hits.recipe_book.model.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {
}
