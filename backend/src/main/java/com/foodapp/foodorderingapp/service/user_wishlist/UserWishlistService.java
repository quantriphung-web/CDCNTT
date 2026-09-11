package com.foodapp.foodorderingapp.service.user_wishlist;

import java.util.List;

import com.foodapp.foodorderingapp.dto.dish.DishRestaurantRes;
import com.foodapp.foodorderingapp.dto.user_wishlist.UserWishlistCreate;
import com.foodapp.foodorderingapp.dto.user_wishlist.UserWishlistRes;
import com.foodapp.foodorderingapp.entity.Dish;
import com.foodapp.foodorderingapp.entity.UserWishlist;

public interface UserWishlistService {
    UserWishlist addToWishlist(UserWishlistCreate userWishlistCreate);
    Boolean removeFromWishlist(long id);
    List<UserWishlistRes> getWishlist();
}
