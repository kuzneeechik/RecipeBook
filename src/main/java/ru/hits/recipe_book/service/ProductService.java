package ru.hits.recipe_book.service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hits.recipe_book.dto.ProductRequest;
import ru.hits.recipe_book.dto.ProductResponse;
import ru.hits.recipe_book.exception.NotFoundException;
import ru.hits.recipe_book.exception.ProductInUseException;
import ru.hits.recipe_book.exception.ValidationException;
import ru.hits.recipe_book.model.CookingRequirement;
import ru.hits.recipe_book.model.DietFlag;
import ru.hits.recipe_book.model.Product;
import ru.hits.recipe_book.model.ProductCategory;
import ru.hits.recipe_book.repository.DishRepository;
import ru.hits.recipe_book.repository.ProductRepository;

@Service
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final DishRepository dishRepository;

    public ProductService(ProductRepository productRepository, DishRepository dishRepository) {
        this.productRepository = productRepository;
        this.dishRepository = dishRepository;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        validateProductNutrition(request.proteins(), request.fats(), request.carbohydrates());
        Product product = new Product();
        applyRequest(product, request);
        return toResponse(productRepository.save(product));
    }

    public List<ProductResponse> findAll(
            ProductCategory category,
            CookingRequirement cookingRequirement,
            Set<DietFlag> flags,
            String search,
            String sortBy,
            String direction
    ) {
        Comparator<Product> comparator = productComparator(sortBy);
        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }
        String normalizedSearch = search == null ? null : search.toLowerCase(Locale.ROOT);
        Set<DietFlag> safeFlags = flags == null ? Set.of() : flags;
        return productRepository.findAll().stream()
                .filter(product -> category == null || product.getCategory() == category)
                .filter(product -> cookingRequirement == null || product.getCookingRequirement() == cookingRequirement)
                .filter(product -> safeFlags.isEmpty() || product.getFlags().containsAll(safeFlags))
                .filter(product -> normalizedSearch == null || product.getName().toLowerCase(Locale.ROOT).contains(normalizedSearch))
                .sorted(comparator)
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse get(UUID id) {
        return toResponse(findProduct(id));
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        validateProductNutrition(request.proteins(), request.fats(), request.carbohydrates());
        Product product = findProduct(id);
        applyRequest(product, request);
        return toResponse(product);
    }

    @Transactional
    public void delete(UUID id) {
        Product product = findProduct(id);
        List<String> dishNames = dishRepository.findByIngredientsProductId(id).stream()
                .map(dish -> dish.getName())
                .toList();
        if (!dishNames.isEmpty()) {
            throw new ProductInUseException(dishNames);
        }
        productRepository.delete(product);
    }

    Product findProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
    }

    ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                List.copyOf(product.getPhotos()),
                product.getCalories(),
                product.getProteins(),
                product.getFats(),
                product.getCarbohydrates(),
                product.getComposition(),
                product.getCategory(),
                product.getCookingRequirement(),
                Set.copyOf(product.getFlags()),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private void applyRequest(Product product, ProductRequest request) {
        product.setName(request.name().trim());
        product.setPhotos(request.photos());
        product.setCalories(request.calories());
        product.setProteins(request.proteins());
        product.setFats(request.fats());
        product.setCarbohydrates(request.carbohydrates());
        product.setComposition(request.composition());
        product.setCategory(request.category());
        product.setCookingRequirement(request.cookingRequirement());
        product.setFlags(request.flags());
    }

    private void validateProductNutrition(double proteins, double fats, double carbohydrates) {
        if (proteins + fats + carbohydrates > 100.0) {
            throw new ValidationException("Proteins, fats and carbohydrates per 100g cannot exceed 100g in total");
        }
    }

    private Comparator<Product> productComparator(String sortBy) {
        return switch (sortBy == null ? "name" : sortBy) {
            case "calories" -> Comparator.comparingDouble(Product::getCalories);
            case "proteins" -> Comparator.comparingDouble(Product::getProteins);
            case "fats" -> Comparator.comparingDouble(Product::getFats);
            case "carbohydrates" -> Comparator.comparingDouble(Product::getCarbohydrates);
            case "name" -> Comparator.comparing(product -> product.getName().toLowerCase(Locale.ROOT));
            default -> throw new ValidationException("Unsupported product sort field: " + sortBy);
        };
    }
}
