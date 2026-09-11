package com.foodapp.foodorderingapp.controller;

import com.foodapp.foodorderingapp.dto.dish.*;
import com.foodapp.foodorderingapp.entity.Dish;
import com.foodapp.foodorderingapp.entity.Dish_GroupOption;
import com.foodapp.foodorderingapp.security.UserPrinciple;
import com.foodapp.foodorderingapp.service.dish.DishService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.internal.bytebuddy.implementation.bind.annotation.Default;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/dishes")
@RequiredArgsConstructor
public class DishController {
    private final DishService dishService;
    
    @GetMapping("category/{restaurantId}/{categoryId}")
    public ResponseEntity<List<DishResponse>> getDishByCategoryId(@PathVariable long restaurantId, @PathVariable long categoryId,  @RequestParam(defaultValue = "10") Integer limit, @RequestParam(defaultValue = "0") int page){
        return ResponseEntity.ok(dishService.getDishesByCategory(restaurantId, categoryId, page, limit));
    }

    @GetMapping("/recommend")
    public ResponseEntity<List<DishResponse>> getRecommendedDishes(@RequestParam List<String> names) throws Exception {
            return ResponseEntity.ok(dishService.getRecommendedDishes(names));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DishResponse> getDishById(@PathVariable long id) throws Exception {
        return ResponseEntity.ok(dishService.getDishById(id));
    }

    @PostMapping
    public ResponseEntity<Dish> createDish(@Valid @RequestBody DishRequest dishRequest) {
        return ResponseEntity.ok(dishService.addDish(dishRequest));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Dish> updateDish(@Valid @RequestBody DishRequest dishRequest, @PathVariable long id) throws Exception {
        return ResponseEntity.ok(dishService.updateDish(id, dishRequest));
    }
    @DeleteMapping
    public ResponseEntity<Boolean> deleteDish(@RequestBody long dishId) throws Exception {
        dishService.deleteDish(dishId);
        return ResponseEntity.ok(true);
    }
    @GetMapping("/search")
    public ResponseEntity<List<DishSearch>> search(@RequestParam String keyword) throws Exception {
        if(keyword.isEmpty()) return ResponseEntity.ok(new ArrayList<>());
        return ResponseEntity.ok(dishService.search(keyword));
    }
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<DishResponse>> getAllByRestaurantId(@PathVariable long restaurantId, @RequestParam(defaultValue = "10") Integer limit, @RequestParam(defaultValue = "0") int page) throws Exception {
        return ResponseEntity.ok(dishService.findDishesByRestaurant(restaurantId, limit, page));
    }
    @GetMapping
    public ResponseEntity<List<Dish>> findAll() throws Exception {
        return ResponseEntity.ok(dishService.findAll());
    }
  

//    @PostMapping("/group_options")
//    public ResponseEntity<Dish_GroupOption> addGroupOptionToDish(@RequestBody Dish_GroupOptionRequest dish_groupOptionRequest) throws Exception {
//        return ResponseEntity.ok( dishService.addGroupOptionToDish(dish_groupOptionRequest.getDishId(), dish_groupOptionRequest.getGroupOptionId()));
//    }
    @PostMapping("/group_options")
    public ResponseEntity<Dish_GroupOption> addGroupOptionToDish(@RequestBody Dish_GroupOptionRequest dish_groupOptionRequest) throws Exception {
        return ResponseEntity.ok( dishService.addGroupOptionToDish(dish_groupOptionRequest.getDishId(), dish_groupOptionRequest.getGroupOptionId()));
    }
//    private void checkUserPermission(long userId){
//        UserPrinciple userInfo = (UserPrinciple) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        if(userInfo.getUserId() !=  userId){
//            throw new IllegalArgumentException("User doesn't have permission");
//        }
//    }


}
