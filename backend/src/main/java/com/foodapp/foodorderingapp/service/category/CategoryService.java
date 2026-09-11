package com.foodapp.foodorderingapp.service.category;

import com.foodapp.foodorderingapp.dto.category.AddDish;
import com.foodapp.foodorderingapp.dto.category.CategoryRequest;
import com.foodapp.foodorderingapp.dto.category.CategoryResponse;
import com.foodapp.foodorderingapp.entity.Category;

import java.util.List;

public interface CategoryService {
    Category createCategory(CategoryRequest category);
    List<Category> getAll();
    Category getCategoryById(long id) throws IllegalArgumentException;
    List<Category> getAllCategories(long restaurantId) throws IllegalArgumentException;
    Category updateCategory(long categoryId, CategoryRequest categoryRequest);

    Category deleteCategory(long id) throws Exception;
    Category addDish(AddDish request);
}
