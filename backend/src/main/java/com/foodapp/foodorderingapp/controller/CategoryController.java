package com.foodapp.foodorderingapp.controller;
import com.foodapp.foodorderingapp.dto.category.AddDish;
import com.foodapp.foodorderingapp.dto.category.CategoryRequest;
import com.foodapp.foodorderingapp.entity.Category;
import com.foodapp.foodorderingapp.service.category.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<Category>> getAll() {
        return ResponseEntity.ok(categoryService.getAll());
    }
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<Category>> getCategoriesByRestaurantId(@PathVariable long restaurantId) {
        return ResponseEntity.ok(categoryService.getAllCategories(restaurantId));
    }
    @PostMapping("/addDish")
    public ResponseEntity<Category> addDish(@Valid @RequestBody AddDish request) {
        return ResponseEntity.ok(categoryService.addDish(request));
    }
    @PostMapping
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        return ResponseEntity.ok(categoryService.createCategory(categoryRequest));
    }
    @PutMapping
    public ResponseEntity<Category> updateCategory(@Valid @RequestBody CategoryRequest categoryRequest){
        return ResponseEntity.ok(categoryService.updateCategory(categoryRequest.getRestaurantId(), categoryRequest));
    }
    @DeleteMapping
    public ResponseEntity<Boolean> deleteCategory(@RequestBody long categoryId) throws Exception {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(true);
    }
}
