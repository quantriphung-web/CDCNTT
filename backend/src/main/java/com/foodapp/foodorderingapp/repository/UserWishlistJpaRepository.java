package com.foodapp.foodorderingapp.repository;

import com.foodapp.foodorderingapp.dto.dish.DishRequest;
import com.foodapp.foodorderingapp.dto.dish.DishRestaurantRes;
import com.foodapp.foodorderingapp.dto.user_wishlist.UserWishlistRes;
import com.foodapp.foodorderingapp.entity.Dish;
import com.foodapp.foodorderingapp.entity.UserWishlist;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@Repository
public interface UserWishlistJpaRepository extends JpaRepository<UserWishlist, Long> {
    @Query("SELECT new com.foodapp.foodorderingapp.dto.user_wishlist.UserWishlistRes(uw.id, d.id, d.price, d.name, d.description, d.imageUrl, d.status) FROM UserWishlist uw, Dish d WHERE uw.userId = ?1 and uw.dishId = d.id") 
    List<UserWishlistRes> findByUserId(long userId);
    @Transactional
    void deleteByUserIdAndDishId(long userId, long dishId);
}
