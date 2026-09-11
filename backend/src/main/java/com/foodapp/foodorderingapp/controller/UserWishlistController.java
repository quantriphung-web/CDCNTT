package com.foodapp.foodorderingapp.controller;
import com.foodapp.foodorderingapp.dto.user_wishlist.UserWishlistCreate;
import com.foodapp.foodorderingapp.dto.user_wishlist.UserWishlistRes;
import com.foodapp.foodorderingapp.entity.UserWishlist;
import com.foodapp.foodorderingapp.service.user_wishlist.UserWishlistService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.http.ResponseEntity;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-wishlist")
public class UserWishlistController {
    private final UserWishlistService userWishlistService;
    public
    @PostMapping()
    ResponseEntity<UserWishlist> addDishToWishlist(@Valid @RequestBody UserWishlistCreate userWishlistCreate){
        UserWishlist userWishlist = userWishlistService.addToWishlist(userWishlistCreate);
        return ResponseEntity.ok(userWishlist);
    }
    public
    @DeleteMapping("/{id}")
    ResponseEntity<Boolean> removeWishlist(@Valid @PathVariable Long id){
        return ResponseEntity.ok(userWishlistService.removeFromWishlist(id));
    }

    public
    @GetMapping("/me")
    ResponseEntity<List<UserWishlistRes>> getWíshlist(){
        return ResponseEntity.ok(userWishlistService.getWishlist());
    }
}
