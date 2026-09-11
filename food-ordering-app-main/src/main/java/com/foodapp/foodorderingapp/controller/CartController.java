package com.foodapp.foodorderingapp.controller;
import com.foodapp.foodorderingapp.dto.cart.*;
import com.foodapp.foodorderingapp.entity.CartItem;
import com.foodapp.foodorderingapp.security.UserPrinciple;
import com.foodapp.foodorderingapp.service.cart.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    @PostMapping
    public ResponseEntity<CartItemResponse> upsertCart(@Valid @RequestBody CartItemRequest cartItemRequest){
        return ResponseEntity.ok(cartService.upsertCartItem(cartItemRequest));
    }

    @GetMapping("/dish_user")
    public ResponseEntity<List<CartItem>> getByDishAndUser(@Valid @RequestBody CartOfDishRequest request){

        return ResponseEntity.ok(cartService.getCartsByDish(request));
    }
    @GetMapping("/restaurant/{id}")
    public ResponseEntity<List<CartItem>> getByRestaurant(@RequestParam long id){
        long userId = ((UserPrinciple) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserId();
        return ResponseEntity.ok(cartService.getCartByRestaurant(id, userId));
    }
    @GetMapping("/user")
    public ResponseEntity<List<CartItemResponse>> getByUser(){
        return ResponseEntity.ok(cartService.getCartByUser());
    }
    @GetMapping("/restaurants")
    public ResponseEntity<List<RestaurantCartResponse>> getRestaurantOfCart(){
        UserPrinciple userPrinciple = (UserPrinciple) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(cartService.getRestaurantOfCart(userPrinciple.getUserId()));
    }
    @PutMapping ("/{id}")
    public CartItemResponse updateCart(@PathVariable long id, @RequestParam int quantity) throws Exception {
       return cartService.updateCart(quantity, id);
    }
}