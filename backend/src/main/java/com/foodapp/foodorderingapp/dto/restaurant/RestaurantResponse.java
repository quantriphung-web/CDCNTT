package com.foodapp.foodorderingapp.dto.restaurant;

import com.foodapp.foodorderingapp.dto.auth.UserResponse;
import com.foodapp.foodorderingapp.dto.category.CategoryResponse;
import com.foodapp.foodorderingapp.dto.dish.DishResponse;
import com.foodapp.foodorderingapp.entity.Dish;
import com.foodapp.foodorderingapp.enumeration.RestaurantStatus;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RestaurantResponse {
    private Long id;
    private String imageUrl;
    private String menuImageUrl;
    private String photoUrls;
    private String coverImageUrl;
    private String name;
    private String mainDish;
    private RestaurantStatus status;
    private List<CategoryResponse> categories;
    private List<DishResponse> dishes;
    private UserResponse owner;
    private String description;
    private String address;
    private String latitude;
    private String longitude;
    private String locationId;
    private Integer numReviews;
    private String rating;
}

