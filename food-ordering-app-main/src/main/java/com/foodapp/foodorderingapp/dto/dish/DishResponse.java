package com.foodapp.foodorderingapp.dto.dish;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.foodapp.foodorderingapp.dto.category.CategoryResponse;
import com.foodapp.foodorderingapp.dto.dish_type.DishTypeResponse;
import com.foodapp.foodorderingapp.dto.group_option.GroupOptionResponse;
import com.foodapp.foodorderingapp.dto.restaurant.RestaurantResponseForDish;
import com.foodapp.foodorderingapp.entity.*;
import com.foodapp.foodorderingapp.enumeration.DishStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DishResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private String imageUrl;
    @Enumerated(EnumType.STRING)
    private DishStatus status;
    private CategoryResponse category;
    private DishTypeResponse dishType;
    private List<GroupOptionResponse> options;
    private RestaurantResponseForDish restaurant;
    private LocalDateTime createdAt;
}
