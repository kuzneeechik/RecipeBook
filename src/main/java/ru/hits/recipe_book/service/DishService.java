package ru.hits.recipe_book.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hits.recipe_book.dto.CalculatedNutritionResponse;
import ru.hits.recipe_book.dto.DishIngredientRequest;
import ru.hits.recipe_book.dto.DishIngredientResponse;
import ru.hits.recipe_book.dto.DishRequest;
import ru.hits.recipe_book.dto.DishResponse;
import ru.hits.recipe_book.exception.NotFoundException;
import ru.hits.recipe_book.exception.ValidationException;
import ru.hits.recipe_book.model.DietFlag;
import ru.hits.recipe_book.model.Dish;
import ru.hits.recipe_book.model.DishCategory;
import ru.hits.recipe_book.model.DishIngredient;
import ru.hits.recipe_book.model.Product;
import ru.hits.recipe_book.repository.DishRepository;

@Service
@Transactional(readOnly = true)
public class DishService {
    private static final Map<String, DishCategory> CATEGORY_MACROS = new java.util.LinkedHashMap<>();

    static {
        CATEGORY_MACROS.put("!десерт", DishCategory.DESSERT);
        CATEGORY_MACROS.put("!первое", DishCategory.FIRST_COURSE);
        CATEGORY_MACROS.put("!второе", DishCategory.SECOND_COURSE);
        CATEGORY_MACROS.put("!напиток", DishCategory.DRINK);
        CATEGORY_MACROS.put("!салат", DishCategory.SALAD);
        CATEGORY_MACROS.put("!суп", DishCategory.SOUP);
        CATEGORY_MACROS.put("!перекус", DishCategory.SNACK);
    }

    private final DishRepository dishRepository;
    private final ProductService productService;

    public DishService(DishRepository dishRepository, ProductService productService) {
        this.dishRepository = dishRepository;
        this.productService = productService;
    }

    @Transactional
    public DishResponse create(DishRequest request) {
        Dish dish = new Dish();
        applyRequest(dish, request);
        return toResponse(dishRepository.save(dish));
    }

    public List<DishResponse> findAll(DishCategory category, Set<DietFlag> flags, String search) {
        String normalizedSearch = search == null ? null : search.toLowerCase(Locale.ROOT);
        Set<DietFlag> safeFlags = flags == null ? Set.of() : flags;
        return dishRepository.findAll().stream()
                .filter(dish -> category == null || dish.getCategory() == category)
                .filter(dish -> safeFlags.isEmpty() || dish.getFlags().containsAll(safeFlags))
                .filter(dish -> normalizedSearch == null || dish.getName().toLowerCase(Locale.ROOT).contains(normalizedSearch))
                .sorted(Comparator.comparing(dish -> dish.getName().toLowerCase(Locale.ROOT)))
                .map(this::toResponse)
                .toList();
    }

    public DishResponse get(UUID id) {
        return toResponse(findDish(id));
    }

    @Transactional
    public DishResponse update(UUID id, DishRequest request) {
        Dish dish = findDish(id);
        applyRequest(dish, request);
        return toResponse(dish);
    }

    @Transactional
    public void delete(UUID id) {
        dishRepository.delete(findDish(id));
    }

    public CalculatedNutritionResponse calculate(List<DishIngredientRequest> ingredientRequests) {
        List<DishIngredient> ingredients = buildIngredients(ingredientRequests);
        Nutrition nutrition = calculateNutrition(ingredients);
        return new CalculatedNutritionResponse(nutrition.calories(), nutrition.proteins(), nutrition.fats(), nutrition.carbohydrates());
    }

    private Dish findDish(UUID id) {
        return dishRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dish not found: " + id));
    }

    private void applyRequest(Dish dish, DishRequest request) {
        MacroResult macroResult = extractFirstMacro(request.name());
        DishCategory category = request.category() == null ? macroResult.category() : request.category();
        if (category == null) {
            throw new ValidationException("Dish category is required if the name does not contain a category macro");
        }

        List<DishIngredient> ingredients = buildIngredients(request.ingredients());
        Nutrition calculated = calculateNutrition(ingredients);
        double calories = request.calories() == null ? calculated.calories() : request.calories();
        double proteins = request.proteins() == null ? calculated.proteins() : request.proteins();
        double fats = request.fats() == null ? calculated.fats() : request.fats();
        double carbohydrates = request.carbohydrates() == null ? calculated.carbohydrates() : request.carbohydrates();
        validateDishNutritionPer100g(proteins, fats, carbohydrates, request.servingSizeGrams());

        Set<DietFlag> availableFlags = calculateAvailableFlags(ingredients);
        Set<DietFlag> safeFlags = request.flags() == null ? Set.of() : request.flags();
        Set<DietFlag> applicableFlags = safeFlags.stream()
                .filter(availableFlags::contains)
                .collect(Collectors.toSet());

        dish.setName(macroResult.nameWithoutMacro().trim());
        dish.setPhotos(request.photos());
        dish.setCalories(calories);
        dish.setProteins(proteins);
        dish.setFats(fats);
        dish.setCarbohydrates(carbohydrates);
        dish.setIngredients(ingredients);
        dish.setServingSizeGrams(request.servingSizeGrams());
        dish.setCategory(category);
        dish.setFlags(applicableFlags);
    }

    private List<DishIngredient> buildIngredients(List<DishIngredientRequest> requests) {
        Map<UUID, Double> quantitiesByProduct = requests.stream()
                .collect(Collectors.toMap(
                        DishIngredientRequest::productId,
                        DishIngredientRequest::quantityGrams,
                        Double::sum
                ));
        List<DishIngredient> ingredients = new ArrayList<>();
        quantitiesByProduct.forEach((productId, quantity) -> {
            Product product = productService.findProduct(productId);
            DishIngredient ingredient = new DishIngredient();
            ingredient.setProduct(product);
            ingredient.setQuantityGrams(quantity);
            ingredients.add(ingredient);
        });
        return ingredients;
    }

    private Nutrition calculateNutrition(List<DishIngredient> ingredients) {
        double calories = 0.0;
        double proteins = 0.0;
        double fats = 0.0;
        double carbohydrates = 0.0;
        for (DishIngredient ingredient : ingredients) {
            double ratio = ingredient.getQuantityGrams() / 100.0;
            Product product = ingredient.getProduct();
            calories += product.getCalories() * ratio;
            proteins += product.getProteins() * ratio;
            fats += product.getFats() * ratio;
            carbohydrates += product.getCarbohydrates() * ratio;
        }
        return new Nutrition(round(calories), round(proteins), round(fats), round(carbohydrates));
    }

    private Set<DietFlag> calculateAvailableFlags(List<DishIngredient> ingredients) {
        return Set.of(DietFlag.values()).stream()
                .filter(flag -> ingredients.stream().allMatch(ingredient -> ingredient.getProduct().getFlags().contains(flag)))
                .collect(Collectors.toSet());
    }

    private void validateDishNutritionPer100g(double proteins, double fats, double carbohydrates, double servingSizeGrams) {
        double macroSumPer100g = (proteins + fats + carbohydrates) / servingSizeGrams * 100.0;
        if (macroSumPer100g > 100.0) {
            throw new ValidationException("Proteins, fats and carbohydrates per 100g of dish cannot exceed 100g in total");
        }
    }

    private MacroResult extractFirstMacro(String rawName) {
        String lowerName = rawName.toLowerCase(Locale.ROOT);
        String selectedMacro = null;
        int selectedIndex = Integer.MAX_VALUE;
        DishCategory selectedCategory = null;
        for (Map.Entry<String, DishCategory> entry : CATEGORY_MACROS.entrySet()) {
            int index = lowerName.indexOf(entry.getKey());
            if (index >= 0 && index < selectedIndex) {
                selectedMacro = entry.getKey();
                selectedIndex = index;
                selectedCategory = entry.getValue();
            }
        }
        if (selectedMacro == null) {
            return new MacroResult(rawName, null);
        }
        String nameWithoutMacro = rawName.replaceFirst("(?iu)" + java.util.regex.Pattern.quote(selectedMacro), "").replaceAll("\\s+", " ");
        return new MacroResult(nameWithoutMacro, selectedCategory);
    }

    private DishResponse toResponse(Dish dish) {
        List<DishIngredientResponse> ingredients = dish.getIngredients().stream()
                .map(ingredient -> new DishIngredientResponse(
                        ingredient.getProduct().getId(),
                        ingredient.getProduct().getName(),
                        ingredient.getQuantityGrams()
                ))
                .toList();
        return new DishResponse(
                dish.getId(),
                dish.getName(),
                List.copyOf(dish.getPhotos()),
                dish.getCalories(),
                dish.getProteins(),
                dish.getFats(),
                dish.getCarbohydrates(),
                ingredients,
                dish.getServingSizeGrams(),
                dish.getCategory(),
                Set.copyOf(dish.getFlags()),
                calculateAvailableFlags(dish.getIngredients()),
                dish.getCreatedAt(),
                dish.getUpdatedAt()
        );
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record Nutrition(double calories, double proteins, double fats, double carbohydrates) {
    }

    private record MacroResult(String nameWithoutMacro, DishCategory category) {
    }
}
