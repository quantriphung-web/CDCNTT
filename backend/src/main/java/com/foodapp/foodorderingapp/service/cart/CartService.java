package com.foodapp.foodorderingapp.service.cart;

import com.foodapp.foodorderingapp.dto.cart.*;
import com.foodapp.foodorderingapp.dto.order.OrderRequest;
import com.foodapp.foodorderingapp.entity.CartItem;
import com.foodapp.foodorderingapp.entity.Order;

import java.util.List;
import java.util.Optional;

public interface CartService {
    CartItemResponse upsertCartItem(CartItemRequest cartItemRequest);
    boolean removeFromCart(long cartId) throws Exception;
    List<CartItem> getCartsByDish(CartOfDishRequest request);
    List<CartItemResponse> getCartByUser();
    List<CartItem> getCartByRestaurant(long restaurantId, long userId);

    CartItemResponse updateCart(int quantity, long id);
    List<RestaurantCartResponse> getRestaurantOfCart(long userId);
}
