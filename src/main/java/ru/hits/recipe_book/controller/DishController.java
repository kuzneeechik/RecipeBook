package ru.hits.recipe_book.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.hits.recipe_book.dto.CalculatedNutritionResponse;
import ru.hits.recipe_book.dto.DishIngredientRequest;
import ru.hits.recipe_book.dto.DishRequest;
import ru.hits.recipe_book.dto.DishResponse;
import ru.hits.recipe_book.model.DietFlag;
import ru.hits.recipe_book.model.DishCategory;
import ru.hits.recipe_book.service.DishService;

@RestController
@RequestMapping("/api/dishes")
public class DishController {
    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @PostMapping
    public ResponseEntity<DishResponse> create(@Valid @RequestBody DishRequest request) {
        DishResponse response = dishService.create(request);
        return ResponseEntity.created(URI.create("/api/dishes/" + response.id())).body(response);
    }

    @GetMapping
    public List<DishResponse> findAll(
            @RequestParam(required = false) DishCategory category,
            @RequestParam(required = false) Set<DietFlag> flags,
            @RequestParam(required = false) String search
    ) {
        return dishService.findAll(category, flags, search);
    }

    @GetMapping("/{id}")
    public DishResponse get(@PathVariable UUID id) {
        return dishService.get(id);
    }

    @PutMapping("/{id}")
    public DishResponse update(@PathVariable UUID id, @Valid @RequestBody DishRequest request) {
        return dishService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        dishService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/calculate")
    public CalculatedNutritionResponse calculate(@Valid @RequestBody List<@Valid DishIngredientRequest> ingredients) {
        return dishService.calculate(ingredients);
    }
}
