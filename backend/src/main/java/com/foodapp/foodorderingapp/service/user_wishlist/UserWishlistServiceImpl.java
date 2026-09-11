package com.foodapp.foodorderingapp.service.user_wishlist;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.foodapp.foodorderingapp.dto.dish.DishRestaurantRes;
import com.foodapp.foodorderingapp.dto.user_wishlist.UserWishlistCreate;
import com.foodapp.foodorderingapp.dto.user_wishlist.UserWishlistRes;
import com.foodapp.foodorderingapp.entity.Dish;
import com.foodapp.foodorderingapp.entity.User;
import com.foodapp.foodorderingapp.entity.UserWishlist;
import com.foodapp.foodorderingapp.repository.DishJpaRepository;
import com.foodapp.foodorderingapp.repository.ProductDiscountJpaRepository;
import com.foodapp.foodorderingapp.repository.UserJpaRepository;
import com.foodapp.foodorderingapp.repository.UserWishlistJpaRepository;
import com.foodapp.foodorderingapp.repository.VoucherApplicationJpaRepository;
import com.foodapp.foodorderingapp.security.UserPrinciple;
import com.foodapp.foodorderingapp.service.dish.DishService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserWishlistServiceImpl implements UserWishlistService {
    private final UserWishlistJpaRepository userWishlistJpaRepository;
    private final DishService dishService;

    @Override
    public UserWishlist addToWishlist(UserWishlistCreate userWishlistCreate) {
        Long userId = ((UserPrinciple)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserId();
        UserWishlist userWishlist = new UserWishlist(userId, userWishlistCreate.getDishId());
        userWishlistJpaRepository.save(userWishlist);
        return userWishlist;
    }

    @Override
    public Boolean removeFromWishlist(long id) {
        try {
            
            userWishlistJpaRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            System.err.println("Failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<UserWishlistRes> getWishlist() {
        Long userId = ((UserPrinciple)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserId();
        List<UserWishlistRes> res = userWishlistJpaRepository.findByUserId(userId);
        return res.stream().map(item -> {
            List<String> imageUrls = dishService.fetchImageUrls(item.getName());
            item.setImageUrl(String.join(", ", imageUrls));
            return item;
        }).collect(Collectors.toList());
    }
    
}
