package com.foodapp.foodorderingapp.repository;

import com.foodapp.foodorderingapp.dto.dish_type.DishTypeOverview;
import com.foodapp.foodorderingapp.entity.DishType;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DishTypeJpaRepository extends JpaRepository<DishType, Long> {
    @Query("SELECT DISTINCT dt FROM DishType dt LEFT JOIN FETCH dt.dishes")
    List<DishType> findTypeWithFeaturedDishes();
    
    @Query("SELECT new com.foodapp.foodorderingapp.dto.dish_type.DishTypeOverview(dt.id, dt.name) FROM DishType dt")
    List<DishTypeOverview> findAllOverviews();
}
