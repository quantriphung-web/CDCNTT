package com.foodapp.foodorderingapp.repository;

import com.foodapp.foodorderingapp.entity.Category;
import com.foodapp.foodorderingapp.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryJpaRepository extends JpaRepository<Category, Long> {
    List<Category> findCategoriesByRestaurantId(long restaurantId);
}
