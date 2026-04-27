package ru.hits.recipe_book.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "dishes")
public class Dish {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ElementCollection
    @CollectionTable(name = "dish_photos", joinColumns = @JoinColumn(name = "dish_id"))
    @Column(name = "photo_url", nullable = false)
    private List<String> photos = new ArrayList<>();

    @Column(nullable = false)
    private double calories;

    @Column(nullable = false)
    private double proteins;

    @Column(nullable = false)
    private double fats;

    @Column(nullable = false)
    private double carbohydrates;

    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DishIngredient> ingredients = new ArrayList<>();

    @Column(nullable = false)
    private double servingSizeGrams;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DishCategory category;

    @ElementCollection
    @CollectionTable(name = "dish_flags", joinColumns = @JoinColumn(name = "dish_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "flag", nullable = false)
    private Set<DietFlag> flags = new HashSet<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos == null ? new ArrayList<>() : new ArrayList<>(photos);
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getProteins() {
        return proteins;
    }

    public void setProteins(double proteins) {
        this.proteins = proteins;
    }

    public double getFats() {
        return fats;
    }

    public void setFats(double fats) {
        this.fats = fats;
    }

    public double getCarbohydrates() {
        return carbohydrates;
    }

    public void setCarbohydrates(double carbohydrates) {
        this.carbohydrates = carbohydrates;
    }

    public List<DishIngredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<DishIngredient> ingredients) {
        this.ingredients.clear();
        if (ingredients != null) {
            ingredients.forEach(this::addIngredient);
        }
    }

    public void addIngredient(DishIngredient ingredient) {
        ingredient.setDish(this);
        this.ingredients.add(ingredient);
    }

    public double getServingSizeGrams() {
        return servingSizeGrams;
    }

    public void setServingSizeGrams(double servingSizeGrams) {
        this.servingSizeGrams = servingSizeGrams;
    }

    public DishCategory getCategory() {
        return category;
    }

    public void setCategory(DishCategory category) {
        this.category = category;
    }

    public Set<DietFlag> getFlags() {
        return flags;
    }

    public void setFlags(Set<DietFlag> flags) {
        this.flags = flags == null ? new HashSet<>() : new HashSet<>(flags);
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
