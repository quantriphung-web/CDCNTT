package com.foodapp.foodorderingapp.dto.user_wishlist;

import java.math.BigDecimal;

import com.foodapp.foodorderingapp.enumeration.DishStatus;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class UserWishlistRes {
    private long id;
    private long dishId;
    private String name;
    private BigDecimal price;
    private String description;
    private String imageUrl;
    @Enumerated(EnumType.STRING)
    private DishStatus status;
    public UserWishlistRes(Long id, long dishId, BigDecimal price, String name, String description, String imageUrl, DishStatus status) {
        this.id = id;
        this.price = price;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.status = status;
        this.dishId = dishId;
    }
}
