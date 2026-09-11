package com.foodapp.foodorderingapp.dto.dish;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.foodapp.foodorderingapp.dto.category.CategoryResponse;
import com.foodapp.foodorderingapp.dto.dish_type.DishTypeResponse;
import com.foodapp.foodorderingapp.entity.Category;
import com.foodapp.foodorderingapp.entity.Dish;
import com.foodapp.foodorderingapp.entity.DishType;
import com.foodapp.foodorderingapp.entity.Restaurant;
import com.foodapp.foodorderingapp.enumeration.DishStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DishRestaurantRes {
    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private String imageUrl;
    @Enumerated(EnumType.STRING)
    private DishStatus status;
    public DishRestaurantRes(Long id, BigDecimal price, String name, String description, String imageUrl, DishStatus status) {
        this.id = id;
        this.price = price;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.status = status;
    }
}
