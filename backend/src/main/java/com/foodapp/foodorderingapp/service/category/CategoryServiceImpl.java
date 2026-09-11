package com.foodapp.foodorderingapp.service.category;

import com.foodapp.foodorderingapp.dto.category.AddDish;
import com.foodapp.foodorderingapp.dto.category.CategoryRequest;
import com.foodapp.foodorderingapp.dto.category.CategoryResponse;
import com.foodapp.foodorderingapp.entity.Category;
import com.foodapp.foodorderingapp.entity.Dish;
import com.foodapp.foodorderingapp.entity.Restaurant;
import com.foodapp.foodorderingapp.repository.CategoryJpaRepository;
import com.foodapp.foodorderingapp.repository.DishJpaRepository;
import com.foodapp.foodorderingapp.repository.RestaurantJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService{
    private final CategoryJpaRepository categoryJpaRepository;
    private final RestaurantJpaRepository restaurantJpaRepository;
    private final DishJpaRepository dishJpaRepository;

    @Override
    @Transactional
    public Category createCategory(CategoryRequest categoryRequest) {
        Optional<Restaurant> restaurant = restaurantJpaRepository.findById(categoryRequest.getRestaurantId());
        if(restaurant.isEmpty()){
            throw new IllegalArgumentException("Not found restaurant");
        }
        Category newCategory = Category
                .builder()
                .name(categoryRequest.getName())
                .restaurant(restaurant.get())
                .dishQuantity(0)
                .isActive(true)
                .build();
        return categoryJpaRepository.save(newCategory);
    }

    @Override
    public List<Category> getAll() {
        return categoryJpaRepository.findAll();
    }

    @Override
    public Category getCategoryById(long id) {
        return categoryJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Override
    public List<Category> getAllCategories(long restaurantId) {
         return categoryJpaRepository.findCategoriesByRestaurantId(restaurantId);
    }

    @Override
    @Transactional
    public Category updateCategory(long categoryId, CategoryRequest categoryRequest) {
        Category existingCategory = getCategoryById(categoryId);
        existingCategory.setName(categoryRequest.getName());
        existingCategory.setActive(categoryRequest.isActive());
        categoryJpaRepository.save(existingCategory);
        return existingCategory;
    }

    @Override
    @Transactional
    public Category deleteCategory(long id) throws Exception {
        Category category = categoryJpaRepository.findById(id)
                .orElseThrow(ChangeSetPersister.NotFoundException::new);

        List<Dish> dishes = new ArrayList<>();
        if (!dishes.isEmpty()) {
            throw new IllegalStateException("Cannot delete category with associated dishes");
        } else {
            categoryJpaRepository.deleteById(id);
            return  category;
        }
    }
    @Override
    public Category addDish(AddDish request) {
        Category category = categoryJpaRepository.findById(request.getCategoryId()).orElseThrow(()-> new EntityNotFoundException("not found category"));
        Dish dish = dishJpaRepository.findById(request.getDishId()).orElseThrow(()-> new EntityNotFoundException("not found dish"));
        category.getDishes().add(dish);
        return categoryJpaRepository.save(category);
    }
}
